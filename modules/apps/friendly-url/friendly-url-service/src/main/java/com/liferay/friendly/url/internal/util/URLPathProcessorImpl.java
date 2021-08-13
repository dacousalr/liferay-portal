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

package com.liferay.friendly.url.internal.util;

import com.liferay.friendly.url.kernel.util.URLPathProcessor;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.VirtualLayoutConstants;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.InactiveRequestHandler;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.util.PropsValues;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Daniel Sanz
 */
@Component(immediate = true, service = URLPathProcessor.class)
public class URLPathProcessorImpl implements URLPathProcessor {

	@Override
	public Group getGroup(HttpServletRequest httpServletRequest)
		throws PortalException {

		return getGroup(httpServletRequest, getPathInfo(httpServletRequest));
	}

	@Override
	public Group getGroup(HttpServletRequest httpServletRequest, String path)
		throws PortalException {

		return getGroup(
			path, getGroupFriendlyURL(path),
			PortalInstances.getCompanyId(httpServletRequest));
	}

	@Override
	public Group getGroup(String path, String friendlyURL, long companyId)
		throws NoSuchGroupException {

		Group group = groupLocalService.fetchFriendlyURLGroup(
			companyId, friendlyURL);

		if (group == null) {
			String screenName = friendlyURL.substring(1);

			User user = userLocalService.fetchUserByScreenName(
				companyId, screenName);

			if (user != null) {
				group = user.getGroup();
			}
			else if (_log.isWarnEnabled()) {
				_log.warn("No user exists with friendly URL " + screenName);
			}
		}

		if ((group == null) ||
			(!group.isActive() &&
			 !inactiveRequestHandler.isShowInactiveRequestMessage() &&
			 !path.startsWith(GroupConstants.CONTROL_PANEL_FRIENDLY_URL) &&
			 !path.startsWith(
				 friendlyURL +
					 VirtualLayoutConstants.CANONICAL_URL_SEPARATOR))) {

			throw new NoSuchGroupException(
				StringBundler.concat(
					"{companyId=", companyId, ", friendlyURL=", friendlyURL,
					"}"));
		}

		return group;
	}

	@Override
	public String getGroupFriendlyURL(String path) {
		String friendlyURL = path;

		int pos = path.indexOf(CharPool.SLASH, 1);

		if (pos != -1) {
			friendlyURL = path.substring(0, pos);
		}

		return friendlyURL;
	}

	@Override
	public String getPathInfo(HttpServletRequest httpServletRequest) {
		String servletPath = httpServletRequest.getServletPath();

		String friendlyURLPathPrefix = portal.getPathFriendlyURLPublic();

		if (servletPath.equals(
				PropsValues.
					LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) ||
			servletPath.startsWith(portal.getPathFriendlyURLPrivateGroup())) {

			friendlyURLPathPrefix = portal.getPathFriendlyURLPrivateGroup();
		}
		else if (servletPath.equals(
					PropsValues.
						LAYOUT_FRIENDLY_URL_PRIVATE_USER_SERVLET_MAPPING) ||
				 servletPath.startsWith(
					 portal.getPathFriendlyURLPrivateUser())) {

			friendlyURLPathPrefix = portal.getPathFriendlyURLPrivateUser();
		}

		String proxyPath = portal.getPathProxy();

		int pathInfoOffset =
			friendlyURLPathPrefix.length() - proxyPath.length();

		return getPathInfo(httpServletRequest, pathInfoOffset);
	}

	@Override
	public String getPathInfo(
		HttpServletRequest httpServletRequest, int pathInfoOffset) {

		String requestURI = httpServletRequest.getRequestURI();

		int pos = requestURI.indexOf(Portal.JSESSIONID);

		if (pos == -1) {
			return requestURI.substring(pathInfoOffset);
		}

		return requestURI.substring(pathInfoOffset, pos);
	}

	@Reference
	protected GroupLocalService groupLocalService;

	@Reference
	protected InactiveRequestHandler inactiveRequestHandler;

	@Reference
	protected Portal portal;

	@Reference
	protected UserLocalService userLocalService;

	private static final Log _log = LogFactoryUtil.getLog(
		URLPathProcessorImpl.class);

}