package org.dashevo.dpp.identity

import org.dashevo.dpp.Fixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdentityTest {

    @Test
    fun testIdentity() {
        var identity = Fixtures.getIdentityFixture()

        assertEquals("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", identity.id)
        assertEquals(2, identity.publicKeys.size)
        assertEquals("AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di", identity.publicKeys[0].data)
        assertEquals(true, identity.publicKeys[1].isEnabled)
    }

    @Test
    fun testIdentityFactory() {

        val factory = IdentityFactory()

        val fixtureCreatedIdentity = Fixtures.getIdentityFixture()

        val publicKeys = ArrayList<IdentityPublicKey>(2)
        publicKeys.add(IdentityPublicKey(0, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di", true))
        publicKeys.add(IdentityPublicKey(2, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "A8AK95PYMVX5VQKzOhcVQRCUbc9pyg3RiL7jttEMDU+L", true))

        val factoryCreatedIdentity = factory.create("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", Identity.IdentityType.USER, publicKeys)

        assertEquals(fixtureCreatedIdentity.id, factoryCreatedIdentity.id)
        assertEquals(fixtureCreatedIdentity.publicKeys[0].data, factoryCreatedIdentity.publicKeys[0].data)
        assertEquals(fixtureCreatedIdentity.findPublicKeyById(2), factoryCreatedIdentity.findPublicKeyById(2))

    }

    @Test
    fun applyStateTransition() {
        val createTransition = Fixtures.getIdentityCreateSTFixture()

        val factory =  IdentityFactory()

        val identity = factory.applyCreateStateTransition(createTransition)

        assertEquals("ylrObex3KikHd5h/13AW/P0yklpCyEOJt7X70cAmVOE", identity.id)
    }
}