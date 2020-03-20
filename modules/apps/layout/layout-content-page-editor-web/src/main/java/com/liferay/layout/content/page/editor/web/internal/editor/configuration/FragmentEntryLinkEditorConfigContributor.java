/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.content.page.editor.web.internal.editor.configuration;

import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.portal.kernel.editor.configuration.BaseEditorConfigContributor;
import com.liferay.portal.kernel.editor.configuration.EditorConfigContributor;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Pavel Savinov
 */
@Component(
	property = {
		"editor.config.key=fragmenEntryLinkEditor",
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET
	},
	service = {
		EditorConfigContributor.class,
		FragmentEntryLinkEditorConfigContributor.class
	}
)
public class FragmentEntryLinkEditorConfigContributor
	extends BaseEditorConfigContributor {

	@Override
	public void populateConfigJSONObject(
		JSONObject jsonObject, Map<String, Object> inputEditorTaglibAttributes,
		ThemeDisplay themeDisplay,
		RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

		jsonObject.put(
			"allowedContent", ""
		).put(
			"disallowedContent", "br"
		).put(
			"documentBrowseLinkUrl", itemSelectorURL.toString()	
		).put(
			"enterMode", 2
		).put(
			"extraPlugins", getExtraPluginsLists()
		).put(
			"removePlugins", getRemovePluginsLists()
		).put(
			"toolbars", JSONFactoryUtil.createJSONObject()
		);
	}

	protected String getExtraPluginsLists() {
		return "ae_autolink,ae_dragresize,ae_addimages,ae_imagealignment," +
			"ae_placeholder,ae_selectionregion,ae_tableresize," +
				"ae_tabletools,ae_uicore";
	}

	protected String getRemovePluginsLists() {
		return "contextmenu,elementspath,floatingspace,image,link,liststyle," +
			"magicline,resize,tabletools,toolbar,ae_embed";
	}
}