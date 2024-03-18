package com.dhl.demp.dmac.domain.deeplink_parser

import android.net.Uri
import com.dhl.demp.dmac.model.DeepLinkAction
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkParserImplTest {
    @Test
    fun `test deep link parsing`() {
        val sut = DeepLinkParserImpl("dmac")

        assertNull(
            sut.parseDeepLink(Uri.parse("dmac://"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi?install=false&uninstall_required=false"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = true, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi?install=true&uninstall_required=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi?install=true&uninstall_required=false"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi?install=false&uninstall_required=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi?install=true"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://com.dhl.demp.dmacapi?uninstall_required=true"))
        )
    }

    @Test
    fun `test parsing deep link with apps`() {
        val sut = DeepLinkParserImpl("dmac")

        assertNull(
            sut.parseDeepLink(Uri.parse("dmac://apps"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi?install=false&uninstall_required=false"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = true, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi?install=true&uninstall_required=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi?install=true&uninstall_required=false"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi?install=false&uninstall_required=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi?install=true"))
        )

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("dmac://apps/com.dhl.demp.dmacapi?uninstall_required=true"))
        )
    }

    @Test
    fun `test parsing deep link corrupted app ids`() {
        val sut = DeepLinkParserImpl("dmac")

        assertNull(
            sut.parseDeepLink(Uri.parse("dmac://apps/"))
        )
        assertNull(
            sut.parseDeepLink(Uri.parse("dmac://apps/   "))
        )

        assertNull(
            sut.parseDeepLink(Uri.parse("dmac://"))
        )
        assertNull(
            sut.parseDeepLink(Uri.parse("dmac://    "))
        )
    }

    @Test
    fun `test parsing app link action detail`() {
        val sut = DeepLinkParserImpl("dmac")

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=detail"))
        )
    }

    @Test
    fun `test parsing app link action install`() {
        val sut = DeepLinkParserImpl("dmac")

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = false),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = false),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install&uninstall_required=false&open_app=false"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = true, openAfterInstallation = false),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install&uninstall_required=true&open_app=false"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = true, openAfterInstallation = false),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install&uninstall_required=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install&uninstall_required=false&open_app=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = false, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install&open_app=true"))
        )

        assertEquals(
            DeepLinkAction.Install("com.dhl.demp.dmacapi", uninstallBeforeInstallation = true, openAfterInstallation = true),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=install&uninstall_required=true&open_app=true"))
        )
    }

    @Test
    fun `test parsing app link action open`() {
        val sut = DeepLinkParserImpl("dmac")

        assertEquals(
            DeepLinkAction.Open("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=open"))
        )
    }

    @Test
    fun `test parsing app link corrupted app ids`() {
        val sut = DeepLinkParserImpl("dmac")

        assertNull(
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac?action=detail"))
        )
        assertNull(
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/   ?action=install"))
        )
        assertNull(
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac?action=open"))
        )
    }

    @Test
    fun `test parsing app link no action`() {
        val sut = DeepLinkParserImpl("dmac")

        assertEquals(
            DeepLinkAction.ShowDetails("com.dhl.demp.dmacapi"),
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi"))
        )
    }

    @Test
    fun `test parsing app link unknown action`() {
        val sut = DeepLinkParserImpl("dmac")

        assertNull(
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac/com.dhl.demp.dmacapi?action=unknown"))
        )
    }

    @Test
    fun `test parsing app link with no parameters`() {
        val sut = DeepLinkParserImpl("dmac")

        assertNull(
            sut.parseDeepLink(Uri.parse("https://applinks.dhl.com/dmac"))
        )
    }
}