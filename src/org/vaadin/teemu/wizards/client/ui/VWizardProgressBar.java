package org.vaadin.teemu.wizards.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VWizardProgressBar extends FlowPanel implements Paintable,
        ProgressBarItemClickedCallback {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-wizardprogressbar";

    private static String combinedStylename = "";

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    ApplicationConnection client;

    private Element barElement;
    private CellPanel captions;

    private String id;
    private boolean initialized = false;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VWizardProgressBar() {
        // This method call of the Paintable interface sets the component
        // style name in DOM tree

    }

    @Override
    public void setStyleName(String style) {
        // TODO Auto-generated method stub
        super.setStyleName(combinedStylename);
    }

    private void init(boolean horizontal, boolean showProgressIndicatorBar) {
        if (horizontal) {
            captions = new HorizontalPanel();
            captions.setWidth("100%");
            combinedStylename = CLASSNAME + " wiz-horiz";
            setStyleName(combinedStylename);
        } else {
            int maxWidth = getOffsetWidth();
            captions = new VerticalPanel();
            // captions.setHeight("100%");
            if (showProgressIndicatorBar) {
                captions.setWidth((maxWidth - 15) + "px");
            } else {
                captions.setWidth((maxWidth) + "px");
            }
            combinedStylename = CLASSNAME + " wiz-vertical";
            addStyleName(combinedStylename);
        }
        add(captions);

        if (showProgressIndicatorBar) {
            Element barWrapperElement = DOM.createDiv();
            barWrapperElement.setClassName("bar-wrapper");
            getElement().appendChild(barWrapperElement);

            barElement = DOM.createDiv();
            barElement.setClassName("bar");
            barWrapperElement.appendChild(barElement);
        }

        initialized = true;
    }

    /**
     * Called whenever an update is received from the server
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // This call should be made first.
        // It handles sizes, captions, tooltips, etc. automatically.
        if (client.updateComponent(this, uidl, true)) {
            // If client.updateComponent returns true there has been no changes
            // and we
            // do not need to update anything.
            return;
        }

        // Save reference to server connection object to be able to send
        // user interaction later
        this.client = client;
        id = uidl.getId();

        // Save the client side identifier (paintable id) for the widget
        paintableId = uidl.getId();

        int offsetWidth = getOffsetWidth();
        int offsetHeight = getOffsetHeight();

        boolean completed = uidl.getBooleanAttribute("complete");
        boolean isHorizontal = uidl.getBooleanAttribute("horizontalbar");
        boolean hasVerticalSpacing = uidl
                .getBooleanAttribute("verticalspacing");
        boolean showProgressIndicatorBar = uidl
                .getBooleanAttribute("showprogress");
        String linkmode = uidl.getStringAttribute("linkmode");

        if (!initialized) {
            init(isHorizontal, showProgressIndicatorBar);
        }

        UIDL steps = uidl.getChildByTagName("steps");
        int numberOfSteps = steps.getChildCount();
        double stepWidth = offsetWidth / (double) numberOfSteps;
        double stepHeight = offsetHeight / (double) numberOfSteps;
        int totalHeight = 0;
        for (int i = 0; i < numberOfSteps; i++) {
            UIDL step = steps.getChildUIDL(i);
            String stepId = step.getStringAttribute("stepid");

            ProgressBarItem item;
            if (captions.getWidgetCount() > i) {
                // get the existing widget for updating
                item = (ProgressBarItem) captions.getWidget(i);
            } else {
                // create new widget and add it to the layout
                item = new ProgressBarItem(i + 1, stepId, this);
                captions.add(item);
            }

            boolean clickableStep = isLinkStep(linkmode,
                    step.getBooleanAttribute("current"),
                    step.getBooleanAttribute("completed"));
            item.setAsLink(clickableStep);
            item.setCaption(step.getStringAttribute("caption"));
            int captionHeight = item.getCaptionElement().getOffsetHeight();
            totalHeight += captionHeight;

            // update the barElement width according to the current step
            if (showProgressIndicatorBar && !completed
                    && step.getBooleanAttribute("current")) {
                if (isHorizontal) {
                    barElement.getStyle().setWidth(
                            (i + 1) * stepWidth - stepWidth / 2, Unit.PX);
                } else {

                    if (hasVerticalSpacing) {
                        barElement.getStyle().setHeight(
                                (i + 1) * stepHeight - stepHeight
                                        + captionHeight / 2, Unit.PX);
                    } else {
                        barElement.getStyle().setHeight(
                                totalHeight - captionHeight / 2, Unit.PX);
                    }
                }
            }
            if (isHorizontal) {
                item.setWidth(stepWidth + "px");
            } else {
                if (hasVerticalSpacing) {
                    item.setHeight(stepHeight + "px");
                }
            }

            boolean first = (i == 0);
            boolean last = (i == steps.getChildCount() - 1);
            updateStyleNames(step, item, first, last, linkmode);
        }

        if (showProgressIndicatorBar && completed) {
            if (isHorizontal) {
                barElement.getStyle().setWidth(100, Unit.PCT);
            } else {
                barElement.getStyle().setHeight(100, Unit.PCT);
            }
        }
    }

    private boolean isLinkStep(String linkmode, boolean isCurrentStep,
            boolean isCompletedStep) {
        if (linkmode.equals("none")) {
            return false;
        }

        if (linkmode.equals("previous") && !isCurrentStep && isCompletedStep) {
            return true;
        }
        if (linkmode.equals("all") && !isCurrentStep) {
            return true;
        }

        return false;
    }

    private void updateStyleNames(UIDL step, ProgressBarItem item,
            boolean first, boolean last, String linkmode) {
        if (step.getBooleanAttribute("completed")) {
            item.addStyleName("completed");
        } else {
            item.removeStyleName("completed");
        }
        if (step.getBooleanAttribute("current")) {
            item.addStyleName("current");
        } else {
            item.removeStyleName("current");
        }
        if (first) {
            item.addStyleName("first");
        } else {
            item.removeStyleName("first");
        }
        if (last) {
            item.addStyleName("last");
        } else {
            item.removeStyleName("last");
        }

        if (linkmode.equals("all")) {
            item.addStyleName("all-linkmode");
        } else {
            item.removeStyleName("all-linkmode");
        }
    }

    private static class ProgressBarItem extends Widget {

        private final int index;
        private Element captionElement;
        private boolean asLink;

        public ProgressBarItem(int index, final String stepId,
                final ProgressBarItemClickedCallback callBack) {
            Element root = Document.get().createDivElement();
            setElement(root);
            setStyleName("step");
            this.index = index;
            captionElement = Document.get().createDivElement();
            root.appendChild(captionElement);

            addDomHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    if (asLink) {
                        callBack.ProgressBarItemClicked(stepId);
                    } else {
                        System.out.println("Clicked, but not as a link");
                    }

                }
            }, ClickEvent.getType());
        }

        public void setCaption(String caption) {

            captionElement.setClassName("step-caption");
            captionElement.setInnerHTML("<span>" + index + ".</span> "
                    + caption);

        }

        protected Element getCaptionElement() {
            return captionElement;
        }

        public boolean isAsLink() {
            return asLink;
        }

        public void setAsLink(boolean asLink) {
            this.asLink = asLink;
            if (asLink) {
                addStyleName("link");
            } else {
                removeStyleName("link");
            }
        }
    }

    @Override
    public void ProgressBarItemClicked(String progressBarItemId) {
        client.updateVariable(id, "pbitemid", progressBarItemId, true);
    }

}

interface ProgressBarItemClickedCallback {
    void ProgressBarItemClicked(String progressBarItemId);
}
