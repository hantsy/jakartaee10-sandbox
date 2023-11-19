package com.example;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.View;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.*;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static jakarta.faces.application.StateManager.IS_BUILDING_INITIAL_STATE;

@View("/hello-facelet2.xhtml")
@ApplicationScoped
public class HelloFacelet2 extends Facelet {
    private static final Logger LOGGER = Logger.getLogger(HelloFacelet2.class.getName());

    @Inject
    Hello hello;

    @Override
    public void apply(FacesContext facesContext, UIComponent root) throws IOException {
        if (!facesContext.getAttributes().containsKey(IS_BUILDING_INITIAL_STATE)) {
            return;
        }

        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();

        ComponentBuilder components = new ComponentBuilder(facesContext);
        List<UIComponent> rootChildren = root.getChildren();

        UIOutput output = new UIOutput();
        output.setValue("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        rootChildren.add(output);

        HtmlBody body = components.create(HtmlBody.COMPONENT_TYPE);
        rootChildren.add(body);

        var title = new UIOutput();
        title.setValue("<h1>Facelets View written in Java(using EL value/method binding)</h1>");
        body.getChildren().add(title);

        HtmlForm form = components.create(HtmlForm.COMPONENT_TYPE);
        form.setId("form");
        form.setPrependId(false);
        body.getChildren().add(form);

        HtmlOutputText message = components.create(HtmlOutputText.COMPONENT_TYPE);
        message.setId("message");
        message.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{hello.message}", String.class));
        //form.getChildren().add(message);

        HtmlInputText name = components.create(HtmlInputText.COMPONENT_TYPE);
        name.setId("name");
        name.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{hello.name}", String.class));
        form.getChildren().add(name);

        HtmlCommandButton actionButton = components.create(HtmlCommandButton.COMPONENT_TYPE);
        actionButton.setId("button");
        actionButton.setActionExpression(expressionFactory.createMethodExpression(elContext, "#{hello.createMessage()}", Void.class, null));
        actionButton.setValue("Say Hello");
        form.getChildren().add(actionButton);

        var br = new UIOutput();
        br.setValue("<br/>");
        form.getChildren().add(br);
        form.getChildren().add(message);

        output = new UIOutput();
        output.setValue("</html>");
        rootChildren.add(output);
    }

    private static class ComponentBuilder {
        FacesContext facesContext;

        ComponentBuilder(FacesContext facesContext) {
            this.facesContext = facesContext;
        }

        @SuppressWarnings("unchecked")
        <T> T create(String componentType) {
            return (T) facesContext.getApplication().createComponent(facesContext, componentType, null);
        }
    }
}
