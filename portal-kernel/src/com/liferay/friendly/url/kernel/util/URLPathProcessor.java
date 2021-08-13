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

package com.liferay.friendly.url.kernel.util;

import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Daniel Sanz
 */
public interface URLPathProcessor {

	public List<Locale> getCompatibleLayoutLocales(Layout layout, String path)
		throws PortalException;

	public Group getGroup(HttpServletRequest httpServletRequest)
		throws PortalException;

	public Group getGroup(HttpServletRequest httpServletRequest, String path)
		throws PortalException;

	public Group getGroup(String path, String groupFriendlyURL, long companyId)
		throws NoSuchGroupException;

	public String getGroupFriendlyURL(String path);

	public Layout getLayout(HttpServletRequest httpServletRequest)
		throws PortalException;

	public Layout getLayout(HttpServletRequest httpServletRequest, Group group)
		throws PortalException;

	public Locale getLocaleFromLayoutURL(HttpServletRequest httpServletRequest)
		throws PortalException;

	public String getPathInfo(HttpServletRequest httpServletRequest);

	public String getPathInfo(
		HttpServletRequest httpServletRequest, int pathInfoOffset);

}