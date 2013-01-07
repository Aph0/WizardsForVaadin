package org.vaadin.teemu.wizards;

import java.util.Map;

import org.vaadin.teemu.wizards.Wizard.LinkMode;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * WizardProgressBar displays the progress bar for a {@link Wizard}.
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.teemu.wizards.client.ui.VWizardProgressBar.class)
public class WizardProgressBar extends AbstractComponent implements
        WizardProgressListener {

    private final Wizard wizard;
    private boolean completed;
    private final boolean hasHorizontalWizardProgressBar;
    private boolean showProgressIndicator = true;

    public WizardProgressBar(Wizard wizard,
            boolean horizontalWizardProgressBar, boolean showProgressIndicator) {
        this.wizard = wizard;
        this.showProgressIndicator = showProgressIndicator;
        hasHorizontalWizardProgressBar = horizontalWizardProgressBar;
        if (horizontalWizardProgressBar) {
            setWidth("100%");
        } else {
            if (wizard.hasVerticalStepSpacing) {
                setHeight("100%");
            } else {
                setHeight(null);
            }
            // This must be in pixels, because the content on the right side is
            // 100% and expanded. This cannot either be undefined, since the
            // captions try to take as much width as possible at the client
            // widget
            setWidth("150px");
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        /*-
         steps
             step
                 caption
                 completed
                 current
             step
         steps
         */
        target.addAttribute("horizontalbar", hasHorizontalWizardProgressBar);
        target.addAttribute("verticalspacing", wizard.hasVerticalStepSpacing);
        target.addAttribute("showprogress", showProgressIndicator);
        if (wizard.currentLinkmode == LinkMode.NONE) {
            target.addAttribute("linkmode", "none");
        } else if (wizard.currentLinkmode == LinkMode.PREVIOUS) {
            target.addAttribute("linkmode", "previous");
        } else if (wizard.currentLinkmode == LinkMode.ALL) {
            target.addAttribute("linkmode", "all");
        }
        target.startTag("steps");
        for (WizardStep step : wizard.getSteps()) {
            target.startTag("step");
            target.addAttribute("caption", step.getCaption());
            if (wizard.getId(step) == null) {
                throw new IllegalStateException("A step's Id cannot be null");
            }
            target.addAttribute("stepid", wizard.getId(step));
            target.addAttribute("completed", wizard.isCompleted(step));
            target.addAttribute("current", wizard.isActive(step));
            target.endTag("step");
        }
        target.endTag("steps");
        target.addAttribute("complete", completed);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if (variables.containsKey("pbitemid")) {
            wizard.activateStep((String) variables.get("pbitemid"));
        }
    }

    public void activeStepChanged(WizardStepActivationEvent event) {
        requestRepaint();
    }

    public void stepSetChanged(WizardStepSetChangedEvent event) {
        requestRepaint();
    }

    public void wizardCompleted(WizardCompletedEvent event) {
        completed = true;
        requestRepaint();
    }

    public void wizardCancelled(WizardCancelledEvent event) {
        // NOP, no need to react to cancellation
    }

    public void setPixelWidth(int pixels) {
        setWidth(pixels + "px");
    }

}
