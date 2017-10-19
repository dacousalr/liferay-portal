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

package com.liferay.portal.util;

import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.test.LayoutTestUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Sergio González
 */
public class PortalImplAlternateURLTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_defaultLocale = LocaleUtil.getDefault();
		_defaultPrependStyle = PropsValues.LOCALE_PREPEND_FRIENDLY_URL_STYLE;

		LocaleUtil.setDefault(
			LocaleUtil.US.getLanguage(), LocaleUtil.US.getCountry(),
			LocaleUtil.US.getVariant());

		TestPropsUtil.set(
			com.liferay.portal.kernel.util.PropsKeys.
				LOCALE_PREPEND_FRIENDLY_URL_STYLE,
			GetterUtil.getString(_defaultPrependStyle));
	}

	@AfterClass
	public static void tearDownClass() {
		LocaleUtil.setDefault(
			_defaultLocale.getLanguage(), _defaultLocale.getCountry(),
			_defaultLocale.getVariant());
	}

	@Test
	public void testCustomPortalLocaleAlternateURL() throws Exception {
		testAlternateURL("localhost", null, null, LocaleUtil.SPAIN, "/es");
	}

	@Test
	public void testCustomPortalLocaleAlternateURL2() throws Exception {
		testAlternateURL("localhost", null, null, LocaleUtil.SPAIN, "/es", "2");
	}

	@Test
	public void testDefaultPortalLocaleAlternateURL() throws Exception {
		testAlternateURL(
			"localhost", null, null, LocaleUtil.US, StringPool.BLANK);
	}

	@Test
	public void testDefaultPortalLocaleAlternateURL2() throws Exception {
		testAlternateURL(
			"localhost", null, null, LocaleUtil.US, StringPool.BLANK, "2");
	}

	@Test
	public void testLocalizedSiteCustomSiteLocaleAlternateURL()
		throws Exception {

		testAlternateURL(
			"localhost",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.US, "/en");
	}

	@Test
	public void testLocalizedSiteCustomSiteLocaleAlternateURL2()
		throws Exception {

		testAlternateURL(
			"localhost",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.US, "/en", "2");
	}

	@Test
	public void testLocalizedSiteDefaultSiteLocaleAlternateURL()
		throws Exception {

		testAlternateURL(
			"localhost",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.SPAIN, StringPool.BLANK);
	}

	@Test
	public void testLocalizedSiteDefaultSiteLocaleAlternateURL2()
		throws Exception {

		testAlternateURL(
			"localhost",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.SPAIN, StringPool.BLANK, "2");
	}

	@Test
	public void testNonlocalhostCustomPortalLocaleAlternateURL()
		throws Exception {

		testAlternateURL("liferay.com", null, null, LocaleUtil.SPAIN, "/es");
	}

	@Test
	public void testNonlocalhostCustomPortalLocaleAlternateURL2()
		throws Exception {

		testAlternateURL("liferay.com", null, null, LocaleUtil.SPAIN, "/es", "2");
	}

	@Test
	public void testNonlocalhostDefaultPortalLocaleAlternateURL()
		throws Exception {

		testAlternateURL(
			"liferay.com", null, null, LocaleUtil.US, StringPool.BLANK);
	}

	@Test
	public void testNonlocalhostDefaultPortalLocaleAlternateURL2()
		throws Exception {

		testAlternateURL(
			"liferay.com", null, null, LocaleUtil.US, StringPool.BLANK, "2");
	}

	@Test
	public void testNonlocalhostLocalizedSiteCustomSiteLocaleAlternateURL()
		throws Exception {

		testAlternateURL(
			"liferay.com",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.US, "/en");
	}

	@Test
	public void testNonlocalhostLocalizedSiteCustomSiteLocaleAlternateURL2()
		throws Exception {

		testAlternateURL(
			"liferay.com",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.US, "/en", "2");
	}

	@Test
	public void testNonlocalhostLocalizedSiteDefaultSiteLocaleAlternateURL()
		throws Exception {

		testAlternateURL(
			"liferay.com",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.SPAIN, StringPool.BLANK);
	}

	@Test
	public void testNonlocalhostLocalizedSiteDefaultSiteLocaleAlternateURL2()
		throws Exception {

		testAlternateURL(
			"liferay.com",
			Arrays.asList(LocaleUtil.US, LocaleUtil.SPAIN, LocaleUtil.GERMANY),
			LocaleUtil.SPAIN, LocaleUtil.SPAIN, StringPool.BLANK, "2");
	}

	protected String generateAssetPublisherContentURL(
		String portalDomain, String languageId, String groupFriendlyURL) {

		StringBundler sb = new StringBundler(11);

		sb.append("http://");
		sb.append(portalDomain);
		sb.append(languageId);
		sb.append(PropsValues.LAYOUT_FRIENDLY_URL_PUBLIC_SERVLET_MAPPING);
		sb.append(Portal.FRIENDLY_URL_SEPARATOR);
		sb.append("asset_publisher");
		sb.append(groupFriendlyURL);
		sb.append(StringPool.FORWARD_SLASH);
		sb.append(StringPool.CONTENT);
		sb.append(StringPool.FORWARD_SLASH);
		sb.append("content-title");

		return sb.toString();
	}

	protected String generateURL(
		String portalDomain, String languageId, String groupFriendlyURL,
		String layoutFriendlyURL) {

		StringBundler sb = new StringBundler(6);

		sb.append("http://");
		sb.append(portalDomain);
		sb.append(languageId);
		sb.append(PropsValues.LAYOUT_FRIENDLY_URL_PUBLIC_SERVLET_MAPPING);
		sb.append(groupFriendlyURL);
		sb.append(layoutFriendlyURL);

		return sb.toString();
	}

	protected ThemeDisplay getThemeDisplay(Group group, String portalURL)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		Company company = CompanyLocalServiceUtil.getCompany(
			TestPropsValues.getCompanyId());

		themeDisplay.setCompany(company);

		themeDisplay.setLayoutSet(group.getPublicLayoutSet());
		themeDisplay.setPortalDomain(HttpUtil.getDomain(portalURL));
		themeDisplay.setPortalURL(portalURL);

		return themeDisplay;
	}

	protected void testAlternateURL(
			String portalDomain, Collection<Locale> groupAvailableLocales,
			Locale groupDefaultLocale, Locale alternateLocale,
			String expectedI18nPath)
		throws Exception {

		testAlternateURL(
			portalDomain, groupAvailableLocales, groupDefaultLocale,
			alternateLocale, expectedI18nPath,
			GetterUtil.getString(_defaultPrependStyle));
	}

	protected void testAlternateURL(
			String portalDomain, Collection<Locale> groupAvailableLocales,
			Locale groupDefaultLocale, Locale alternateLocale,
			String expectedI18nPath, String prependStyle)
		throws Exception {

		TestPropsUtil.set(
			com.liferay.portal.kernel.util.PropsKeys.
				LOCALE_PREPEND_FRIENDLY_URL_STYLE,
			prependStyle);

		_group = GroupTestUtil.addGroup();

		_group = GroupTestUtil.updateDisplaySettings(
			_group.getGroupId(), groupAvailableLocales, groupDefaultLocale);

		Layout layout = LayoutTestUtil.addLayout(
			_group.getGroupId(), "welcome", false);

		String canonicalURL = generateURL(
			portalDomain, StringPool.BLANK, _group.getFriendlyURL(),
			layout.getFriendlyURL());

		String actualAlternateURL = PortalUtil.getAlternateURL(
			canonicalURL, getThemeDisplay(_group, canonicalURL),
			alternateLocale, layout);

		String expectedAlternateURL = generateURL(
			portalDomain, expectedI18nPath, _group.getFriendlyURL(),
			layout.getFriendlyURL());

		Assert.assertEquals(expectedAlternateURL, actualAlternateURL);

		String canonicalAssetPublisherContentURL =
			generateAssetPublisherContentURL(
				portalDomain, StringPool.BLANK, _group.getFriendlyURL());

		String actualAssetPublisherContentAlternateURL =
			PortalUtil.getAlternateURL(
				canonicalAssetPublisherContentURL,
				getThemeDisplay(_group, canonicalAssetPublisherContentURL),
				alternateLocale, layout);

		String expectedAssetPublisherContentAlternateURL =
			generateAssetPublisherContentURL(
				portalDomain, expectedI18nPath, _group.getFriendlyURL());

		Assert.assertEquals(
			expectedAssetPublisherContentAlternateURL,
			actualAssetPublisherContentAlternateURL);
	}

	private static Locale _defaultLocale;
	private static int _defaultPrependStyle;

	@DeleteAfterTestRun
	private Group _group;

}