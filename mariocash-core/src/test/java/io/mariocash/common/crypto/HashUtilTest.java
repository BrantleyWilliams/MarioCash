package dev.zhihexireng.common.crypto;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class HashUtilTest {

    private static final Logger log = LoggerFactory.getLogger(HashUtilTest.class);

    @Before
    public void setUp() {
        //todo: change test vectors to NISTs.
    }


    @Test
    public void SHA1StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA-1"));
        log.info(result1);
        assertEquals(result1, "0114498021cb8c4f1519f96bdf58dd806f3adb63");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA-1"));
        log.info(result2);
        assertEquals(result2, "07a1a50a6273e6bc2eb94d647810cdc5b275b924");
    }

    @Test
    public void SHA224StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA-224"));
        log.info(result1);
        assertEquals(result1, "fc5d6aed7146d6747dd6fca075f9fe5a30a4c0c9ff67effc484f10b5");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA-224"));
        log.info(result2);
        assertEquals(result2, "820518c626796d9af2db299dfb37c8737d8f56f12c44fb99b7aece54");
    }

    @Test
    public void SHA256StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA-256"));
        log.info(result1);
        assertEquals(result1, "60e05bd1b195af2f94112fa7197a5c88289058840ce7c6df9693756bc6250f55");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA-256"));
        log.info(result2);
        assertEquals(result2, "3138bb9bc78df27c473ecfd1410f7bd45ebac1f59cf3ff9cfe4db77aab7aedd3");
    }

    @Test
    public void SHA384StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA-384"));
        log.info(result1);
        assertEquals(result1, "53871b26a08e90cb62142f2a39f0b80de41792322b0ca560"
                + "2b6eb7b5cf067c49498a7492bb9364bbf90f40c1c5412105");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA-384"));
        log.info(result2);
        assertEquals(result2, "751a419cb935b79162930b839109e7b40d06e4a09332bd44"
                + "8f2b089478096e99a1d0c820c31a7aa92a35bfe9e6113425");
    }

    @Test
    public void SHA512StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA-512"));
        log.info(result1);
        assertEquals(result1, "8f6beb3c0792f50c176800332f4468f76b4457b41d2f68e294cb46e53addbf57"
                + "69a59eddf33e19394e8ab78e374b1bd33a680d26464fcd1174da226af9c8cd6e");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA-512"));
        log.info(result2);
        assertEquals(result2, "6723a63fc813efa037dab2128781cbc395a90ffd83bf2b520d6d62488350d898"
                + "fd5624717ac2fa443388cb80fb7a784a04aa4fa6659c4fcce87e62dec718bb95");
    }

    @Test
    public void RIPEMD160StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "RIPEMD160"));
        log.info(result1);
        assertEquals(result1, "fd2bead7cf387c7896e2f42926fca4b4a0483d88");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "RIPEMD160"));
        log.info(result2);
        assertEquals(result2, "b2b0034d91c88d857e5c7164086343291c0d9be8");
    }

    @Test
    public void Keccak256StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "KECCAK-256"));
        log.info(result1);
        assertEquals(result1, "d874d9e5ad41e13e8908ab82802618272c3433171cdc3d634f3b1ad0e6742827");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "KECCAK-256"));
        log.info(result2);
        assertEquals(result2, "01b2a6c0dd38f8fb49ea3594776c584a74321ecebe87a4e885636153c96f79fc");
    }

    @Test
    public void Keccak512StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "KECCAK-512"));
        log.info(result1);
        assertEquals(result1, "b4c84e84c5bac30ab535c0ed496c178d093ada704342da9d16f9f16bda957f18"
                + "79a000627f8b0958eb3805803c0a9da269f71d611437334053828309cab6f1bb");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "KECCAK-512"));
        log.info(result2);
        assertEquals(result2, "f82a967b743fe010df792d3fa843bbb826c04fecd6d3bc36aa6c40a9fb452ffa"
                + "0e841af94547129c572d5150d37f58909b03c882ac5a18e3cc0b72e9aa73e32b");
    }

    @Test
    public void SHA3_224StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA3-224"));
        log.info(result1);
        assertEquals(result1, "ad5c4adcaa5ae42d9ba3ef45f530b7165e1705dd4eb78ef8ab2f8bba");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA3-224"));
        log.info(result2);
        assertEquals(result2, "a130f28dc1ab0f7ece1383db81476cdc2ed9818b81002c2438c3e883");
    }

    @Test
    public void SHA3_256StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA3-256"));
        log.info(result1);
        assertEquals(result1, "c6fdd7a7f70862b36a26ccd14752268061e98103299b28fe7763bd9629926f4b");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA3-256"));
        log.info(result2);
        assertEquals(result2, "a3284ba81d18dfa82dbe17b7a8af3321ec406ff4f264e26d70fd88a870913686");
    }

    @Test
    public void SHA3_384StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA3-384"));
        log.info(result1);
        assertEquals(result1, "4c3578fa9e31872b06a2f3cdbd91470591f963fa6c38d76c"
                + "4754970b60a1d9c77fc2adf2fdfef804ea77ef6872dd8616");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA3-384"));
        log.info(result2);
        assertEquals(result2, "6a57cf6f631c2beb383cdc5c9d306ba6f57f0584ff8f9306"
                + "9e342b42479625ce5a61e82d1c0f23bc44df941c485ed3d8");
    }

    @Test
    public void SHA3_512StaticTest() {
        String input1 = "0000000000000000000000000000000000000000000000000000000000000000";
        String result1 = Hex.toHexString(HashUtil.hash(input1.getBytes(), "SHA3-512"));
        log.info(result1);
        assertEquals(result1, "27f4caaa1d51d54a53cb2393fa4e24b542a963509055a2a4864816a4d2375d3a"
                + "afd433df86c25a4529503a0c99ab46e97871e573d45de78e9508fe581693694e");

        String input2 = "1111111111111111111111111111111111111111111111111111111111111111";
        String result2 = Hex.toHexString(HashUtil.hash(input2.getBytes(), "SHA3-512"));
        log.info(result2);
        assertEquals(result2, "6aedf8191b2ff5d4c704df5718eeb35a57dc0438a506294d2a528cc5d3aa2d63"
                + "3f5eceb7b49db152fb0b9a4fd981d3659c4a5ee58454825b824569a15e83a2b6");
    }





}
