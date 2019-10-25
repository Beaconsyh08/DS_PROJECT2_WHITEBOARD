package Client;

import com.google.common.hash.Hashing;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

public class DigestUtil {

    public static final String SALT = "geelysdafaqj23ou89ZXcj@#$@#$#@KJdjklj;D../dSF.,";
    public static final Integer DIGEST_TIMES = 1;

    public static final Integer OPEN_API_DIGEST_TIMES = 1;

    public static String digest(String password) {
        return digest(password, SaltUtil.getEncryptTimes());
    }

    public static String digest(String password, int encryptTimes) {
        String salt = SaltUtil.generateSalt();
        return digest(password, salt, encryptTimes);
    }

    public static String digest(String password, String salt, int encryptTimes) {
        MessageDigest digest;
        try {
            digest = DigestUtils.getSha256Digest();

            if (salt != null) {
                digest.update(salt.getBytes("UTF-8"));
            }

            byte[] result = digest.digest(password.getBytes("UTF-8"));

            for (int i = 1; i < encryptTimes; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return Hex.encodeHexString(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha256(String originString) {
        if (Objects.isNull(originString)) {
            return null;
        }
        return Hashing.sha256().hashString(originString, StandardCharsets.UTF_8).toString();
    }
}
