/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.cdi.rebind;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.api.ConversationContext;
import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock
 */
@CodeDecorator
public class ConversationExtension extends IOCDecoratorExtension<ConversationContext> {
    public ConversationExtension(Class<ConversationContext> decoratesWith) {
        super(decoratesWith);
    }

    public String generateDecorator(InjectionPoint<ConversationContext> injectionPoint) {
     //   final InjectionContext ctx = injectionPoint.getInjectionContext();

        final MetaClass eventClassType = MetaClassFactory.get(injectionPoint.getInjectionContext()
            .getProcessingContext().loadClassType(Event.class));

        final MetaField field = injectionPoint.getField();

        if (!eventClassType.isAssignableFrom(field.getType())) {
            throw new RuntimeException("@ConversationContext should be used with type Event");
        }

        final ConversationContext context = field.getAnnotation(ConversationContext.class);

        MetaParameterizedType type = field.getType().getParameterizedType();
        if (type == null) {
            throw new RuntimeException("Event<?> must be parameterized");
        }

        MetaClass typeParm = (MetaClass) type.getTypeParameters()[0];

        String toSubject = CDI.getSubjectNameByType(typeParm.getFullyQualifedName());

        String expression = injectionPoint.getValueExpression()
                + ".registerConversation(" + CDI.class.getName() + ".createConversation(\"" + toSubject + "\"));";
                
        return expression;
    }
}