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

package com.liferay.asset.publisher.web.util;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.publisher.constants.AssetPublisherPortletKeys;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortletConstants;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Objects;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;

/**
 * Provides utility methods to be used from Asset Publisher display templates.
 * This class is injected in the context of Asset Publisher display templates.
 *
 * @author     Juan Fernández
 * @deprecated As of Judson (7.1.x), replaced by {@link
 *             com.liferay.asset.publisher.util.AssetPublisherHelper}
 */
@Deprecated
public class AssetPublisherHelper {

	public static String getAssetViewURL(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse, AssetEntry assetEntry) {

		return getAssetViewURL(
			liferayPortletRequest, liferayPortletResponse, assetEntry, false);
	}

	public static String getAssetViewURL(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse, AssetEntry assetEntry,
		boolean viewInContext) {

		AssetRenderer<?> assetRenderer = assetEntry.getAssetRenderer();

		return getAssetViewURL(
			liferayPortletRequest, liferayPortletResponse, assetRenderer,
			assetEntry, viewInContext);
	}

	public static String getAssetViewURL(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		AssetRenderer<?> assetRenderer, AssetEntry assetEntry,
		boolean viewInContext) {

		PortletURL viewFullContentURL =
			liferayPortletResponse.createRenderURL();

		viewFullContentURL.setParameter("mvcPath", "/view_content.jsp");
		viewFullContentURL.setParameter(
			"assetEntryId", String.valueOf(assetEntry.getEntryId()));

		PortletURL redirectURL = liferayPortletResponse.createRenderURL();

		int cur = ParamUtil.getInteger(liferayPortletRequest, "cur");
		int delta = ParamUtil.getInteger(liferayPortletRequest, "delta");
		boolean resetCur = ParamUtil.getBoolean(
			liferayPortletRequest, "resetCur");

		redirectURL.setParameter("cur", String.valueOf(cur));

		if (delta > 0) {
			redirectURL.setParameter("delta", String.valueOf(delta));
		}

		redirectURL.setParameter("resetCur", String.valueOf(resetCur));
		redirectURL.setParameter(
			"assetEntryId", String.valueOf(assetEntry.getEntryId()));

		viewFullContentURL.setParameter("redirect", redirectURL.toString());

		AssetRendererFactory<?> assetRendererFactory =
			assetRenderer.getAssetRendererFactory();

		viewFullContentURL.setParameter("type", assetRendererFactory.getType());

		ThemeDisplay themeDisplay =
			(ThemeDisplay)liferayPortletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (Validator.isNotNull(assetRenderer.getUrlTitle())) {
			if (assetRenderer.getGroupId() != themeDisplay.getScopeGroupId()) {
				viewFullContentURL.setParameter(
					"groupId", String.valueOf(assetRenderer.getGroupId()));
			}

			viewFullContentURL.setParameter(
				"urlTitle", assetRenderer.getUrlTitle());
		}

		String viewURL = null;

		if (viewInContext) {
			try {
				String noSuchEntryRedirect = viewFullContentURL.toString();

				viewURL = assetRenderer.getURLViewInContext(
					liferayPortletRequest, liferayPortletResponse,
					noSuchEntryRedirect);

				if (Validator.isNotNull(viewURL) &&
					!Objects.equals(viewURL, noSuchEntryRedirect)) {

					viewURL = HttpUtil.setParameter(
						viewURL, "redirect",
						PortalUtil.getCurrentURL(liferayPortletRequest));
				}
			}
			catch (Exception e) {
			}
		}

		if (Validator.isNull(viewURL)) {
			viewURL = viewFullContentURL.toString();
		}

		viewURL = _replacePortletIdIfLinkedToAnotherLayout(
			liferayPortletRequest, viewURL);

		return viewURL;
	}

	private static String _replacePortletIdIfLinkedToAnotherLayout(
		LiferayPortletRequest liferayPortletRequest, String viewUrl) {

		Layout layout = LayoutLocalServiceUtil.fetchLayout(
			liferayPortletRequest.getPlid());

		PortletPreferences portletPreferences =
			liferayPortletRequest.getPreferences();

		String portletSetupLinkToLayoutUuid = portletPreferences.getValue(
			"portletSetupLinkToLayoutUuid", StringPool.BLANK);

		if ((layout != null) &&
			Validator.isNotNull(portletSetupLinkToLayoutUuid)) {

			Layout linkedLayout =
				LayoutLocalServiceUtil.fetchLayoutByUuidAndGroupId(
					portletSetupLinkToLayoutUuid, layout.getGroupId(),
					layout.isPrivateLayout());

			String newPortletId = linkedLayout.getTypeSettingsProperty(
				LayoutTypePortletConstants.DEFAULT_ASSET_PUBLISHER_PORTLET_ID,
				StringPool.BLANK);

			if (Validator.isNotNull(newPortletId)) {
				String prefix =
					AssetPublisherPortletKeys.ASSET_PUBLISHER + "_INSTANCE_";

				String newId = StringUtil.replace(
					newPortletId, prefix, StringPool.BLANK);

				String oldPortletName = liferayPortletRequest.getPortletName();

				String oldId = StringUtil.replace(
					oldPortletName, prefix, StringPool.BLANK);

				viewUrl = StringUtil.replace(
					viewUrl, oldPortletName, prefix + newId);

				viewUrl = StringUtil.replace(
					viewUrl, StringPool.SLASH + oldId + StringPool.SLASH,
					StringPool.SLASH + newId + StringPool.SLASH);
			}
		}

		return viewUrl;
	}

}