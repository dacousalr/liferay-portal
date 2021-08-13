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
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.NoSuchLayoutException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutFriendlyURL;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.VirtualLayoutConstants;
import com.liferay.portal.kernel.portlet.LayoutFriendlyURLSeparatorComposite;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutFriendlyURLLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.InactiveRequestHandler;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.util.PropsValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Daniel Sanz
 */
@Component(immediate = true, service = URLPathProcessor.class)
public class URLPathProcessorImpl implements URLPathProcessor {

	@Override
	public List<Locale> getCompatibleLayoutLocales(Layout layout, String path)
		throws PortalException {

		List<LayoutFriendlyURL> layoutFriendlyURLs =
			layoutFriendlyURLLocalService.getLayoutFriendlyURLs(
				layout.getPlid());

		List<Locale> compatibleLayoutLocales = new ArrayList<>();

		List<Locale> incompatibleLayoutLocales = new ArrayList<>();

		Locale siteDefaultLocale = portal.getSiteDefaultLocale(
			layout.getGroup());

		String defaultFriendlyURL = layout.getFriendlyURL(siteDefaultLocale);

		boolean isDefaultFriendlyURL = false;

		for (LayoutFriendlyURL layoutFriendlyURL : layoutFriendlyURLs) {
			if (path.contains(layoutFriendlyURL.getFriendlyURL())) {
				compatibleLayoutLocales.add(
					LocaleUtil.fromLanguageId(
						layoutFriendlyURL.getLanguageId()));

				String currentLayoutFriendlyURL =
					layoutFriendlyURL.getFriendlyURL();

				if (currentLayoutFriendlyURL.equals(defaultFriendlyURL)) {
					isDefaultFriendlyURL = true;
				}
			}
			else {
				incompatibleLayoutLocales.add(
					LocaleUtil.fromLanguageId(
						layoutFriendlyURL.getLanguageId()));
			}
		}

		if (isDefaultFriendlyURL) {
			Set<Locale> availableLocales = LanguageUtil.getAvailableLocales(
				layout.getGroupId());

			for (Locale siteLocale : availableLocales) {
				if (!siteLocale.equals(siteDefaultLocale) &&
					!incompatibleLayoutLocales.contains(siteLocale)) {

					compatibleLayoutLocales.add(siteLocale);
				}
			}
		}

		return compatibleLayoutLocales;
	}

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
	public Layout getLayout(HttpServletRequest httpServletRequest)
		throws PortalException {

		return getLayout(httpServletRequest, getGroup(httpServletRequest));
	}

	@Override
	public Layout getLayout(HttpServletRequest httpServletRequest, Group group)
		throws PortalException {

		String path = getPathInfo(httpServletRequest);

		int pos = path.indexOf(CharPool.SLASH, 1);

		String friendlyURL = null;

		if ((pos != -1) && ((pos + 1) != path.length())) {
			friendlyURL = path.substring(pos);
		}

		Map<String, Object> requestContext = HashMapBuilder.<String, Object>put(
			"request", httpServletRequest
		).build();

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			serviceContext = ServiceContextFactory.getInstance(
				httpServletRequest);

			ServiceContextThreadLocal.pushServiceContext(serviceContext);
		}

		Map<String, String[]> params = httpServletRequest.getParameterMap();

		boolean privateLayout = _isPrivate(httpServletRequest.getServletPath());

		try {
			LayoutFriendlyURLSeparatorComposite
				layoutFriendlyURLSeparatorComposite =
					portal.getLayoutFriendlyURLSeparatorComposite(
						group.getGroupId(), privateLayout, friendlyURL, params,
						requestContext);

			return layoutFriendlyURLSeparatorComposite.getLayout();
		}
		catch (NoSuchLayoutException noSuchLayoutException) {
			List<Layout> layouts = layoutLocalService.getLayouts(
				group.getGroupId(), privateLayout,
				LayoutConstants.DEFAULT_PARENT_LAYOUT_ID);

			for (Layout layout : layouts) {
				if (layout.matches(httpServletRequest, friendlyURL)) {
					return layout;
				}
			}

			throw noSuchLayoutException;
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	@Override
	public Locale getLocaleFromLayoutURL(HttpServletRequest httpServletRequest)
		throws PortalException {

		Group group = getGroup(httpServletRequest);

		Layout layout = getLayout(httpServletRequest, group);

		if (layout == null) {
			return null;
		}

		String requestURI = _stripContextPath(
			httpServletRequest.getRequestURI());

		List<Locale> compatibleLayoutLocales = getCompatibleLayoutLocales(
			layout, requestURI);

		if (compatibleLayoutLocales.size() != 1) {
			User user = portal.getUser(httpServletRequest);

			if (user != null) {
				Locale userLocale = LocaleUtil.fromLanguageId(
					user.getLanguageId());

				if (compatibleLayoutLocales.contains(userLocale)) {
					return userLocale;
				}
			}

			return portal.getSiteDefaultLocale(group);
		}

		Locale layoutFriendlyURLLocale = compatibleLayoutLocales.get(0);

		if (LanguageUtil.isAvailableLocale(
				group.getGroupId(), layoutFriendlyURLLocale)) {

			return layoutFriendlyURLLocale;
		}

		return null;
	}

	@Override
	public String getPathInfo(HttpServletRequest httpServletRequest) {
		String servletPath = httpServletRequest.getServletPath();

		String friendlyURLPathPrefix = portal.getPathFriendlyURLPublic();

		if (_isPrivateGroup(servletPath)) {
			friendlyURLPathPrefix = portal.getPathFriendlyURLPrivateGroup();
		}
		else if (_isPrivateUser(servletPath)) {
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
	protected LayoutFriendlyURLLocalService layoutFriendlyURLLocalService;

	@Reference
	protected LayoutLocalService layoutLocalService;

	@Reference
	protected Portal portal;

	@Reference
	protected UserLocalService userLocalService;

	private boolean _isPrivate(String servletPath) {
		if (_isPrivateGroup(servletPath) || _isPrivateUser(servletPath)) {
			return true;
		}

		return false;
	}

	private boolean _isPrivateGroup(String servletPath) {
		if (servletPath.equals(
				PropsValues.
					LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) ||
			servletPath.startsWith(portal.getPathFriendlyURLPrivateGroup())) {

			return true;
		}

		return false;
	}

	private boolean _isPrivateUser(String servletPath) {
		if (servletPath.equals(
				PropsValues.LAYOUT_FRIENDLY_URL_PRIVATE_USER_SERVLET_MAPPING) ||
			servletPath.startsWith(portal.getPathFriendlyURLPrivateUser())) {

			return true;
		}

		return false;
	}

	private String _stripContextPath(String requestURI) {
		String contextPath = portal.getPathContext();

		if (Validator.isNotNull(contextPath) &&
			requestURI.startsWith(contextPath)) {

			requestURI = requestURI.substring(contextPath.length());
		}

		return StringUtil.replace(
			requestURI, StringPool.DOUBLE_SLASH, StringPool.SLASH);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		URLPathProcessorImpl.class);

}