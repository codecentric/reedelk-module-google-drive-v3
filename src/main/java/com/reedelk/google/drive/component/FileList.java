package com.reedelk.google.drive.component;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.User;
import com.reedelk.google.drive.internal.DriveService;
import com.reedelk.google.drive.internal.commons.Default;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static com.reedelk.runtime.api.commons.StringUtils.isNotBlank;
import static java.util.stream.Collectors.toList;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google File List")
@Description("The Google File List component lists or searches files from google drive. " +
        "Optional search filters and order by sort keys can be applied to filter and order the returned results. " +
        "If the number of returned files is potentially large it is recommended to use pagination by setting the page size and " +
        "page token for subsequent listings.")
@Component(service = FileList.class, scope = PROTOTYPE)
public class FileList implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Drive Configuration providing the Service Account Credentials file.")
    private DriveConfiguration configuration;

    @Property("Drive ID")
    @Hint("my-drive-name")
    @Example("my-drive-name")
    @Description("ID of the shared drive to search.")
    private String driveId;

    @Property("Page Size")
    @Hint("30")
    @Example("50")
    @DefaultValue("100")
    @Description("The maximum number of files to return per page. " +
            "Partial or empty result pages are possible even before the end of the files " +
            "list has been reached. Acceptable values are 1 to 1000, inclusive. (Default: 100)")
    private Integer pageSize;

    @Property("Page Token")
    @Description("The token for continuing a previous list request on the next page. " +
            "This should be set to the value of 'nextPageToken' from the previous response.")
    private DynamicString pageToken;

    @Property("Filter Query")
    @Hint("name contains 'hello' and name contains 'goodbye'")
    @Example("<br>The following examples shows some basic query strings which can be used in this property:" +
            "<table style=\"width:100%\">" +
            "<tr><th align=\"left\">What you want to query</th><th align=\"left\">Example</th></tr>" +
            "<tr>" +
            "<td>Files with the name \"hello\"</td><td><code>name = 'hello'</code></td>" +
            "</tr>" +
            "<tr>" +
            "<td>Files with a name containing the words \"hello\" and \"goodbye\"</td><td><code>name contains 'hello' and name contains 'goodbye'</code></td>" +
            "</tr>" +
            "<tr>" +
            "<td>Files that contain the word \"hello\"</td><td><code>fullText contains 'hello'</code></td>" +
            "</tr>" +
            "<tr>" +
            "<td>Files for which user \"test@example.org\" has write permission</td><td><code>'test@example.org' in writers</code></td>" +
            "</tr>" +
            "<tr>" +
            "<td>Files modified after a given date</td><td><code>modifiedTime > '2012-06-04T12:00:00' // default time zone is UTC</code></td>" +
            "</tr>" +
            "</table>")
    @Description("A query for filtering the file results. See the <a href=\"https://developers.google.com/drive/api/v3/search-files\">Search for files</a> guide for the supported syntax.")
    private String query;

    @Property("Order By")
    @InitValue("name")
    @Hint("folder,modifiedTime desc,name")
    @Example("folder,modifiedTime desc,name")
    @Description("A comma-separated list of sort keys. " +
            "Valid keys are 'createdTime', 'folder', 'modifiedByMeTime', 'modifiedTime', 'name', " +
            "'name_natural', 'quotaBytesUsed', 'recency', 'sharedWithMeTime', 'starred', and 'viewedByMeTime'. " +
            "Each key sorts ascending by default, but may be reversed with the 'desc' modifier.")
    private String orderBy;

    private Drive drive;
    private int realPageSize;

    @Reference
    private ScriptEngineService scriptEngine;

    @Override
    public void initialize() {
        drive = DriveService.create(FileList.class, configuration);
        realPageSize = Optional.ofNullable(pageSize).orElse(Default.PAGE_SIZE);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        try {

            Drive.Files.List list = drive.files().list();
            list.setPageSize(realPageSize);
            scriptEngine.evaluate(pageToken, flowContext, message)
                    .ifPresent(list::setPageToken);
            if (isNotBlank(driveId)) list.setDriveId(driveId);
            if (isNotBlank(orderBy)) list.setOrderBy(orderBy);
            if (isNotBlank(query)) list.setQ(query);

            com.google.api.services.drive.model.FileList files =
                    list.setFields("*")
                    .execute();
            List<Map<String, Serializable>> mappedFiles = files.getFiles()
                    .stream()
                    .map(FILE_MAPPER)
                    .collect(toList());

            return MessageBuilder.get(FileList.class)
                    .withJavaObject(mappedFiles)
                    .build();
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageToken(DynamicString pageToken) {
        this.pageToken = pageToken;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    private final Function<File, Map<String, Serializable>> FILE_MAPPER = new Function<File, Map<String, Serializable>>() {
        @Override
        public Map<String, Serializable> apply(File file) {
            Map<String, Serializable> map = new HashMap<>();
            map.put("driveId", file.getDriveId());
            map.put("fileExtension", file.getFileExtension());
            map.put("id", file.getId());
            map.put("description", file.getDescription());
            map.put("name", file.getName());
            map.put("originalFilename", file.getOriginalFilename());
            map.put("ownedByMe", file.getOwnedByMe());

            List<Map<String, Serializable>> collect = file.getOwners().stream().map(USER_MAPPER).collect(toList());
            map.put("owners", new ArrayList<>(collect));
            map.put("webViewLink", file.getWebViewLink());
            map.put("webContentLink", file.getWebContentLink());
            return map;
        }
    };

    private final Function<User, Map<String, Serializable>> USER_MAPPER = new Function<User, Map<String, Serializable>>() {
        @Override
        public Map<String, Serializable> apply(User user) {
            Map<String, Serializable> map = new HashMap<>();
            map.put("emailAddress", user.getEmailAddress());
            return map;
        }
    };
}
