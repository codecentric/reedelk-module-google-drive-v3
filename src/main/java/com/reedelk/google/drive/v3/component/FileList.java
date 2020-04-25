package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.command.FileListCommand;
import com.reedelk.google.drive.v3.internal.commons.Default;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Files List")
@Component(service = FileList.class, scope = PROTOTYPE)
@Description("Lists files from Google Drive. " +
        "Optional search filters and order by sort keys can be applied to filter and order the returned results. " +
        "If the number of returned files is potentially large it is recommended to use pagination by setting the page size and page token for subsequent listings." +
        "This component requires the configuration of a Service Account to make authorized API calls " +
        "on behalf of the user. The component's configuration uses the private key (in JSON format) " +
        "of the Google Service Account which can be generated and downloaded from the Service Account page. " +
        "More info about Service Accounts and how they can be created and configured can " +
        "be found in the official Google Service Accounts <a href=\"https://cloud.google.com/iam/docs/service-accounts\">Documentation</a> page.")
public class FileList implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("Drive ID")
    @Hint("my-drive-name")
    @Example("my-drive-name")
    @Description("ID of the shared drive to search for files.")
    private String driveId;

    @Property("Page Size")
    @Group("Paging")
    @Hint("30")
    @Example("50")
    @DefaultValue("100")
    @Description("The maximum number of files to return per page. " +
            "Partial or empty result pages are possible even before the end of the files " +
            "list has been reached. Acceptable values are 1 to 1000, inclusive.")
    private Integer pageSize;

    @Property("Page Token")
    @Group("Paging")
    @InitValue("#[message.attributes().nextPageToken]")
    @DefaultValue("#[message.attributes().nextPageToken]")
    @Hint("Use a dynamic value to set next page token: message.attributes().nextPageToken")
    @Example("<code>message.attributes().nextPageToken</code>")
    @Description("The token for continuing a previous list request on the next page. " +
            "This should be set to the value of 'nextPageToken' from the previous response.")
    @When(propertyName = "pageSize", propertyValue = When.NOT_BLANK)
    private DynamicString nextPageToken;

    @Property("Filter Query")
    @Group("Filter And Order")
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
    private DynamicString query;

    @Property("Order By")
    @Group("Filter And Order")
    @Hint("folder,modifiedTime desc,name")
    @Example("folder,modifiedTime desc,name")
    @Description("A comma-separated list of sort keys. " +
            "Valid keys are 'createdTime', 'folder', 'modifiedByMeTime', 'modifiedTime', 'name', " +
            "'name_natural', 'quotaBytesUsed', 'recency', 'sharedWithMeTime', 'starred', and 'viewedByMeTime'. " +
            "Each key sorts ascending by default, but may be reversed with the 'desc' modifier.")
    private String orderBy;

    @Reference
    ScriptEngineService scriptEngine;

    DriveApi driveApi;

    private int realPageSize;

    @Override
    public void initialize() {
        driveApi = createApi();
        realPageSize = Optional.ofNullable(pageSize).orElse(Default.PAGE_SIZE);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realNextPageToken =
                scriptEngine.evaluate(nextPageToken, flowContext, message).orElse(null);

        String realQuery =
                scriptEngine.evaluate(query, flowContext, message).orElse(null);

        FileListCommand command =
                new FileListCommand(driveId, orderBy, realQuery, realNextPageToken, realPageSize);

        List<Map> driveFiles = driveApi.execute(command);

        return MessageBuilder.get(FileList.class)
                .withList(driveFiles, Map.class)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setNextPageToken(DynamicString nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setQuery(DynamicString query) {
        this.query = query;
    }

    DriveApi createApi() {
        return DriveApiFactory.create(FileList.class, configuration);
    }
}
