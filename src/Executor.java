import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Executor {
    int curDictSize, lenOfreadRawBytes;
    ArrayList<Byte> rawBytes;

    Executor(){
        curDictSize = 0;
        lenOfreadRawBytes = 0;
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
        int countOfBlockDickt = 0;
        /* main loop */
        for (int i = 0; i < byteCount; i++) {
            /* add one byte to buffer */
            buffer.add(bytes[i]);
            /* if dictionary size larger 127 so we should clear this bcs byte can hold to 127 */
            if(dictK.size() == 127){
                out.add(dictK.size() * countOfBlockDickt++ , (byte)dictK.size());
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
                    out.add((byte)0);
                }
                else{
                    //tmp = intToByteArray(dictK.indexOf(buffer) + 1);
                    out.add((byte)(dictK.indexOf(buffer) + 1));
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
        /* if buffer not empty => we add to buffer bytes but didnt processing at the end */
        if (!buffer.isEmpty()) {
            byte[] tmp;
            byte last = buffer.get(buffer.size() - 1);
            buffer.remove(buffer.size() - 1);
            if (dictK.isEmpty() || buffer.size() == 0) {
                //tmp = intToByteArray(0);
                out.add((byte)0);
            }
            else {
                out.add((byte)(dictK.indexOf(buffer) + 1));
                //tmp = intToByteArray(dictK.indexOf(buffer) + 1);
            }
            /*for (int j = 0; j < 4; j++) {
                out.add(tmp[j]);
            }*/
            out.add(last);
        }
        out.add(dictK.size() * countOfBlockDickt , (byte)dictK.size());

        return ListToArrayPrim(out);
    }

    void decodeLZ78(Byte[] bytes, int bytesCount, ArrayList<Byte> out){
        int countReadBytes = 0;
        if(curDictSize == 0){
            curDictSize = (int)bytes[0];
            countReadBytes = 1;
        }



        ArrayList<ArrayList<Byte>> dictK = new ArrayList<>();
        for (int i = 0; i < bytesCount; i++) {
            int pos = (int)bytes[i];//ByteArrToInt(bytes, i);
            i = i+1;//i + 4;
            if(dictK.size() == 127) {
                dictK.clear();
            }
            ArrayList<Byte> tmp = new ArrayList<>();
            if (dictK.size() > pos - 1 && pos != 0) {
                tmp.addAll(dictK.get(pos - 1));
                out.addAll(tmp);
            }

            out.add(bytes[i]);
            tmp.add(bytes[i]);
            dictK.add(new ArrayList<>(tmp));
        }
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
        Byte[] byteToDecode;
        int countReadBytes = 0;

        if(curDictSize == 0){
            curDictSize = (int)bytes[0];
            countReadBytes = 1;
        }

        while(countReadBytes < bytesCount) {
            if (curDictSize - lenOfreadRawBytes > bytesCount) {
                lenOfreadRawBytes += bytesCount;
                for (byte b : bytes) {
                    rawBytes.add(b);
                }
                countReadBytes += bytesCount;
                return null;
            } else {
                for (int i = countReadBytes; i < curDictSize + countReadBytes; i++) {
                    rawBytes.add(bytes[i]);
                }
                countReadBytes += curDictSize;
                byteToDecode = new Byte[rawBytes.size()];
                rawBytes.toArray(byteToDecode);
                decodeLZ78(byteToDecode, curDictSize, out);
                rawBytes.clear();

                for (int i = bytesCount - (curDictSize - lenOfreadRawBytes); i < bytesCount; i++) {
                    rawBytes.add(bytes[i]);
                }
                lenOfreadRawBytes = bytesCount - (curDictSize - lenOfreadRawBytes);
                curDictSize = (int) bytes[curDictSize - lenOfreadRawBytes];
            }
        }

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
    private byte[] ListToArrayPrim(ArrayList<Byte> arr){
        byte[] res = new byte[arr.size()];
        for (int j = 0; j < arr.size(); j++) {
            res[j] = arr.get(j);
        }
        return res;
    }

    /**
     * BRIEF:
     * two last methods only for write to output key of int insted byte (dictionary size may be larger size of byte)
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
