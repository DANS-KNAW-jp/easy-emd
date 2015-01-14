package nl.knaw.dans.easy.web.template;

import static org.apache.wicket.markup.html.CSSPackageResource.getHeaderContribution;

import org.apache.wicket.behavior.HeaderContributor;

public class Style {
    // @formatter:off 
    public static final HeaderContributor

            MODAL_HEADER_CONTRIBUTION = getHeaderContribution(Style.class, "css/modal/modal.css"),
            FILE_EXPLORER_HEADER_CONTRIBUTION = getHeaderContribution(Style.class, "css/fileexplorer/file-explorer.css"),
            USER_SELECTOR_HEADER_CONTRIBUTION = getHeaderContribution(Style.class, "css/user_selector.css"),
            ACTIVITY_LOG_PANEL_CONTRIBUTION = getHeaderContribution(Style.class, "css/activity_log_panel.css"),
            ADMIN_PANEL_CONTRIBUTION = getHeaderContribution(Style.class, "css/admin_panel.css"),
            EASY_HEADER_CONTRIBUTION = getHeaderContribution(Style.class, "css/easy.css"),
            VIEW_DATASET_HEADER_CONTRIBUTION = getHeaderContribution(Style.class, "css/view_dataset.css");
    // @formatter:on 
}
