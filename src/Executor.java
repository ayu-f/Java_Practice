import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Executor {
    // for decompress
    int curDictSize; // current dictionary size
    ArrayList<Byte> rawBytes; // unprocessed bytes
    int countReadBytes; // count of read and processed bytes (index of begin new block to decompress)

    Executor() {
        curDictSize = 0;
        countReadBytes = 0;
        rawBytes = new ArrayList<>();
    }

    /**
     * BRIEF:
     * data compression
     * ARGS:
     * bytes - input array bytes
     * byteCount - count of read bytes
     * RETURN:
     * compression byte array
     */
    public byte[] encode(byte[] bytes, int byteCount) {
        /* dictionary of bytes array */
        ArrayList<ArrayList<Byte>> dictK = new ArrayList<>();
        ArrayList<Byte> buffer = new ArrayList<>();
        /* list for output */
        ArrayList<Byte> out = new ArrayList<>();
        int endBlock = 0, countOfBlockDickt = 0;
        /* main loop */
        for (int i = 0; i < byteCount; i++) {
            /* add one byte to buffer */
            buffer.add(bytes[i]);
            /* if dictionary size larger 127 so we should clear this bcs byte can hold to 127 */
            if (dictK.size() == 10) {
                int a = dictK.size();
                out.add(endBlock, (byte) (dictK.size()));
                endBlock = out.size();
                dictK.clear();
            }
            /* if buffer not contains in dictionary */
            if (!containsKeyDictK(dictK, buffer)) {
                byte[] tmp;
                /* remove last el, so we have longer buffer in dict */
                buffer.remove(buffer.size() - 1);

                /* get index of position buffer in dict */
                if (!dictK.contains(buffer)) {
                    //tmp = intToByteArray(0);
                    out.add((byte) 0);
                } else {
                    //tmp = intToByteArray(dictK.indexOf(buffer) + 1);
                    out.add((byte) (dictK.indexOf(buffer) + 1));
                }

                /*for (int j = 0; j < 4; j++) {
                    out.add(tmp[j]);
                }*/
                /* add current byte in out */
                out.add(bytes[i]);

                /* update dictionary */
                buffer.add(bytes[i]);
                dictK.add(new ArrayList<Byte>(buffer));

                buffer.clear();
            }
        }
        /* if buffer not empty => we add to buffer bytes but didn't process at the end */
        if (!buffer.isEmpty()) {
            byte[] tmp;
            byte last = buffer.get(buffer.size() - 1);
            buffer.remove(buffer.size() - 1);
            if (dictK.isEmpty() || buffer.size() == 0) {
                //tmp = intToByteArray(0);
                out.add((byte) 0);
                dictK.add(new ArrayList<Byte>(new ArrayList<Byte>(0)));
            } else {
                out.add((byte) (dictK.indexOf(buffer) + 1));
                dictK.add(new ArrayList<Byte>(buffer));
                //tmp = intToByteArray(dictK.indexOf(buffer) + 1);
            }
            /*for (int j = 0; j < 4; j++) {
                out.add(tmp[j]);
            }*/
            out.add(last);
        }
        out.add(endBlock, (byte) dictK.size());

        return ListToArrayPrim(out);
    }
    /**
     * algorithm lz78
     */
    ArrayList<Byte> decodeLZ78(byte[] bytes, int bytesCount) {
        ArrayList<Byte> out = new ArrayList<>();
        ArrayList<ArrayList<Byte>> dictK = new ArrayList<>();
        for (int i = countReadBytes + 1; i < bytes.length - 1; i++) {
            int pos = (int) bytes[i];//ByteArrToInt(bytes, i);
            i = i + 1;//i + 4;
            ArrayList<Byte> tmp = new ArrayList<>();
            if (dictK.size() > pos - 1 && pos != 0) {
                tmp.addAll(dictK.get(pos - 1));
                out.addAll(tmp);
            }

            out.add(bytes[i]);
            tmp.add(bytes[i]);
            dictK.add(new ArrayList<>(tmp));
            if (dictK.size() == curDictSize) {
                countReadBytes = i + 1;
                dictK.clear();
                return out;
            }
        }
        return null;
    }

    byte[] MakeDecodeArr(byte[] bytes, int bytesCount) {
        byte[] arr = new byte[rawBytes.size() + bytesCount];
        int i = 0;
        for (byte b : rawBytes) {
            arr[i++] = b;
        }
        for (int j = 0; j < bytesCount; j++) {
            arr[i++] = bytes[j];
        }

        return arr;
    }

    /**
     * BRIEF:
     * data decompression
     * ARGS:
     * bytes - input array bytes
     * byteCount - count of read bytes
     * RETURN:
     * decompression byte array
     */
    public byte[] decode(byte[] bytes, int bytesCount) {
        ArrayList<Byte> out = new ArrayList<>();
        byte[] byteToDecode;


        if (curDictSize == 0) {
            curDictSize = (int) bytes[0];
            //countReadBytes = 1;
        }
        ArrayList<Byte> tmpOut;
        byteToDecode = MakeDecodeArr(bytes, bytesCount);
        do {
            if ((tmpOut = decodeLZ78(byteToDecode, bytesCount)) == null) {
                rawBytes.clear();
                for (int i = countReadBytes; i < byteToDecode.length; i++) {
                    rawBytes.add(byteToDecode[i]);
                }
            } else {
                out.addAll(tmpOut);
                if (countReadBytes == byteToDecode.length)
                    curDictSize = 0;
                else
                    curDictSize = (int) byteToDecode[countReadBytes];
                rawBytes.clear();
                for (int i = countReadBytes; i < byteToDecode.length; i++) {
                    rawBytes.add(byteToDecode[i]);
                }
            }
        } while (tmpOut != null);
        countReadBytes = 0;
        return ListToArrayPrim(out);
    }

    /**
     * BRIEF:
     * checks key is contained in the dictionary
     * ARGS:
     * dictK - dictionary of bytes array
     * key - the key from the bytes array
     * RETURN:
     * true - key is found
     * false - key not found
     */
    boolean containsKeyDictK(ArrayList<ArrayList<Byte>> dictK, ArrayList<Byte> key) {
        for (ArrayList<Byte> el : dictK) {
            if (key.equals(el)) {
                return true;
            }
        }
        return false;
    }

    /**
     * BRIEF:
     * ArrayList of bytes to byte[]
     * ARGS:
     * arr - ArrayList bytes
     * RETURN:
     * res - output byte[]
     */
    private byte[] ListToArrayPrim(ArrayList<Byte> arr) {
        byte[] res = new byte[arr.size()];
        for (int j = 0; j < arr.size(); j++) {
            res[j] = arr.get(j);
        }
        return res;
    }

    /**
     * BRIEF:
     * two last methods only for write to output key of int instead byte (dictionary size may be larger size of byte)
     * first - byte array to int number
     * second - int number to byte array 4 size
     */
    private int ByteArrToInt(byte[] arr, int i) {
        byte[] tmp = new byte[4];
        tmp[0] = arr[i++];
        tmp[1] = arr[i++];
        tmp[2] = arr[i++];
        tmp[3] = arr[i];

        return ByteBuffer.wrap(tmp).getInt();
    }

    private byte[] intToByteArray(int data) {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF));
        return result;
    }

}
