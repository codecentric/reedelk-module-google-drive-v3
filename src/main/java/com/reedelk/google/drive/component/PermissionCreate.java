package com.reedelk.google.drive.component;

import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Google Permission Create")
@Component(service = PermissionCreate.class, scope = PROTOTYPE)
public class PermissionCreate implements ProcessorSync {

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        return null;
    }
}
