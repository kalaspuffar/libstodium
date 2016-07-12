package eu.artemisc.stodium;

import org.abstractj.kalium.SodiumJNI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Jan van de Molengraft [jan@artemisc.eu]
 */
public class Blake2b {
    static {
        // Require sodium_init();
        Stodium.StodiumInit();
    }

    // constants
    public static final int BYTES         = StodiumJNI.crypto_generichash_blake2b_bytes();
    public static final int BYTES_MIN     = StodiumJNI.crypto_generichash_blake2b_bytes_min();
    public static final int BYTES_MAX     = StodiumJNI.crypto_generichash_blake2b_bytes_max();
    public static final int KEYBYTES      = StodiumJNI.crypto_generichash_blake2b_keybytes();
    public static final int KEYBYTES_MIN  = StodiumJNI.crypto_generichash_blake2b_keybytes_min();
    public static final int KEYBYTES_MAX  = StodiumJNI.crypto_generichash_blake2b_keybytes_max();
    public static final int SALTBYTES     = StodiumJNI.crypto_generichash_blake2b_saltbytes();
    public static final int PERSONALBYTES = StodiumJNI.crypto_generichash_blake2b_personalbytes();
    public static final int STATE_BYTES   = StodiumJNI.crypto_generichash_blake2b_statebytes();

    // Implementation of the stream API

    /**
     * state holds the binary representation of the
     * crypto_generichash_blake2b_state value.
     */
    @NotNull private final ByteBuffer state;

    /**
     * outlen is the number of output bytes the state should produce. It is
     * used byte genericHashFinal to validate that the number of
     * output-bytes read from the state is &lt;= State.outlen.
     */
    private final int outlen;

    /**
     * State allocates a byte array that holds the raw packed value of the C
     * crypto_generichash_state bytes. This constructor does NOT call
     * {@code init()}.
     *
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public Blake2b(final int outlen)
            throws StodiumException {
        Stodium.checkSize(outlen, BYTES_MIN, BYTES_MAX, "Blake2b.BYTES_MIN", "Blake2b.BYTES_MAX");
        this.state  = ByteBuffer.allocateDirect(STATE_BYTES);
        this.outlen = outlen;
    }

    /**
     * This constructor calls {@link #init(ByteBuffer)}.
     *
     * @param outlen
     * @param key
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public Blake2b(          final int        outlen,
                   @Nullable final ByteBuffer key)
            throws StodiumException {
        this(outlen);
        init(key);
    }

    /**
     * State copy-constructor. If _finish should be called on multiple
     * occasions during the streaming without losing the state, it can be
     * copied.
     *
     * @param original The original State that should be copied
     */
    public Blake2b(@NotNull final Blake2b original) {
        this.state  = ByteBuffer.allocateDirect(STATE_BYTES);
        this.outlen = original.outlen;

        state.duplicate().put(original.state.duplicate());
    }

    /**
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public void init()
            throws StodiumException {
        init(null);
    }

    /**
     *
     * @param key
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public void init(@Nullable final ByteBuffer key)
            throws StodiumException {
        if (key != null) {
            Stodium.checkSize(key.remaining(), KEYBYTES_MIN, KEYBYTES_MAX,
                    "Blake2b.KEYBYTES_MIN", "Blake2b.KEYBYTES_MAX");
        }

        Stodium.checkStatus(StodiumJNI.crypto_generichash_blake2b_init(
                state,
                key == null ? ByteBuffer.allocateDirect(0) : Stodium.ensureUsableByteBuffer(key),
                outlen));
    }

    /**
     *
     * @param key
     * @param salt
     * @param personal
     * @throws ConstraintViolationException
     * @throws StodiumException
     *
     * FIXME this API should allow null-values for key (at least) and maybe for salt/personal
     */
    /*public void init(@NotNull  final byte[] key,
                     @Nullable final byte[] salt,
                     @Nullable final byte[] personal)
            throws StodiumException {
        Stodium.checkSize(key.length, KEYBYTES_MIN, KEYBYTES_MAX,
                "Blake2b.KEYBYTES_MIN", "Blake2b.KEYBYTES_MAX");

        if (salt != null) {
            Stodium.checkSize(salt.length, SALTBYTES,
                    "Blake2b.SALTBYTES");
        }
        if (personal != null) {
            Stodium.checkSize(personal.length, PERSONALBYTES,
                    "Blake2b.PERSONALBYTES");
        }

        Stodium.checkStatus(StodiumJNI.crypto_generichash_blake2b_init_salt_personal(
                        state, key, key.length, outlen, salt, personal));
    }*/

    /**
     *
     * @param in
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public void update(@NotNull final ByteBuffer in)
            throws StodiumException {
        Stodium.checkStatus(StodiumJNI.crypto_generichash_blake2b_update(
                state, Stodium.ensureUsableByteBuffer(in)));
    }

    /**
     * @param out
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public void doFinal(@NotNull final ByteBuffer out)
            throws StodiumException {
        Stodium.checkSize(out.remaining(), 1, outlen, "1", "Blake2b.outlen");
        Stodium.checkDestinationWritable(out, "Blake2b#doFinal(out)");
        Stodium.checkStatus(StodiumJNI.crypto_generichash_blake2b_final(
                state, Stodium.ensureUsableByteBuffer(out)));
    }

    // wrappers

    //
    // Simple API
    //

    /**
     * genericHash calculates the hash of the input using the key. The result
     * will be placed in dstHash
     *
     * @param dstHash the destination array the hash will be written to
     * @param srcInput the value that will be hashed
     * @param srcKey the key used to calculate the hash
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public static void genericHash(@NotNull  final ByteBuffer dstHash,
                                   @NotNull  final ByteBuffer srcInput,
                                   @Nullable final ByteBuffer srcKey)
            throws StodiumException {
        final Blake2b blake2b = new Blake2b(dstHash.remaining(), srcKey);
        blake2b.update(srcInput);
        blake2b.doFinal(dstHash);
    }

    /**
     * genericHash without key, equivalent to calling
     * {@link #genericHash(ByteBuffer, ByteBuffer, ByteBuffer)} with {@code srcKey == null}
     * or {@code srcKey.length == 0}.
     *
     * @param dstHash the destination array the hash will be written to
     * @param srcInput the value that will be hashed
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    public static void genericHash(@NotNull final ByteBuffer dstHash,
                                   @NotNull final ByteBuffer srcInput)
            throws StodiumException {
        genericHash(dstHash, srcInput, null);
    }

    /**
     *
     * @param dstHash
     * @param srcInput
     * @param key
     * @param salt
     * @param personal
     * @throws ConstraintViolationException
     * @throws StodiumException
     */
    /*public static void genericHashSaltPersonal(@NotNull final ByteBuffer dstHash,
                                               @NotNull final ByteBuffer srcInput,
                                               @NotNull final ByteBuffer key,
                                               @NotNull final ByteBuffer salt,
                                               @NotNull final ByteBuffer personal)
            throws StodiumException {
        final Blake2b blake2b = new Blake2b(dstHash.remaining());
        blake2b.init(key, salt, personal);
        blake2b.update(srcInput);
        blake2b.doFinal(dstHash);
    }*/
}
