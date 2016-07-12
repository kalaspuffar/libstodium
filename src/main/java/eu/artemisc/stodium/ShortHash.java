package eu.artemisc.stodium;

import org.abstractj.kalium.Sodium;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * ShortHash wraps calls to sodium's crypto_shorthash API.
 *
 * @author Jan van de Molengraft [jan@artemisc.eu]
 */
public class ShortHash {
    static {
        // Require sodium_init();
        Stodium.StodiumInit();
    }

    // block the constructor
    private ShortHash() {}

    // constants
    public static final int BYTES = Sodium.crypto_shorthash_bytes();
    public static final int KEYBYTES = Sodium.crypto_shorthash_keybytes();

    public static final String PRIMITIVE = Sodium.crypto_shorthash_primitive();

    /**
     *
     * @param srcIn
     * @param srcKey
     * @return a Long that holds the (BigEndian) representation of the resulting
     *         64-bit Hash value.
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    @NotNull
    static Long shorthash(@NotNull final byte[] srcIn,
                          @NotNull final byte[] srcKey)
            throws StodiumException {
        Stodium.checkSize(srcKey.length, KEYBYTES, "ShortHash.KEYBYTES");

        byte[] dst = new byte[BYTES];
        Stodium.checkStatus(
                Sodium.crypto_shorthash(dst, srcIn, srcIn.length, srcKey));

        // Return as long
        return ByteBuffer.wrap(dst)
                .order(ByteOrder.BIG_ENDIAN)
                .getLong();
    }

    /**
     *
     * @param dstHash The destination array to which the resulting 8-byte hash.
     *                The bytes are considered to be BigEndian.
     * @param srcIn
     * @param srcKey
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    static void shorthash(@NotNull final byte[] dstHash,
                          @NotNull final byte[] srcIn,
                          @NotNull final byte[] srcKey)
            throws StodiumException {
        Stodium.checkSize(dstHash.length, BYTES, "ShortHash.BYTES");
        Stodium.checkSize(srcKey.length, KEYBYTES, "ShortHash.KEYBYTES");
        Stodium.checkStatus(
                Sodium.crypto_shorthash(dstHash, srcIn, srcIn.length, srcKey));
    }
}
