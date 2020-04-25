package com.reedelk.google.drive.v3.component;

import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.command.FileDeleteCommand;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractComponentTest {

    @Mock
    protected Message message;
    @Mock
    protected FlowContext context;
    @Mock
    protected DriveApi driveApi;
    @Mock
    protected ScriptEngineService scriptEngine;
    @Mock
    protected ConverterService converterService;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            DynamicValue<?> dynamicValue = invocation.getArgument(0);
            return Optional.ofNullable(dynamicValue.value());
        }).when(scriptEngine).evaluate(any(DynamicValue.class), eq(context), eq(message));

        lenient().doAnswer(invocation -> invocation.getArgument(0))
                .when(converterService)
                .convert(any(Object.class), any(Class.class));
    }
}
