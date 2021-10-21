import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tmp {
    private boolean isNotFirstBuf = false;
    private int bufSize;
    private byte[] buffer;
    private ArrayList<Short> dictionary;
    private ArrayList<Byte> compressedBuffer;
    private Logger logger;
    private int dictSize = 0;
    private int compBufferSize = 0;

    private ArrayList<Byte> untreatedBytes;
    private int necessarySize = 0;

    Tmp() {
        dictionary = new ArrayList<>();
        compressedBuffer = new ArrayList<>();
    }

    public byte[] compress(byte[] buffer, int byteCount) {
        //int byteCount = 0;
        if (!dictionary.isEmpty() || !compressedBuffer.isEmpty()) {
            dictionary.clear();
            compressedBuffer.clear();
        }

        this.buffer = buffer;
        CreateListOfPairs(byteCount);
        CreateCompressedBuffer(byteCount);
        return createMergedArray();
    }

    private byte[] createMergedArray() {
        int index = 4;
        int dictSize = dictionary.size(), compBufSize = compressedBuffer.size();
        int mergedArraySize = dictSize * 2 + compBufSize + 8;

        if (mergedArraySize % 2 != 0)
            mergedArraySize += 1;


        byte[] mergedArray = new byte[mergedArraySize];
        mergedArray[mergedArraySize - 1] = 0;

        byte[] dictSizeInBytes = intToByteArray(dictSize);

        //Здесь я записываю размер словаря. Так как он в int, то записываю 4 байта
        mergedArray[0] = dictSizeInBytes[0];
        mergedArray[1] = dictSizeInBytes[1];
        mergedArray[2] = dictSizeInBytes[2];
        mergedArray[3] = dictSizeInBytes[3];

        for (short elem : dictionary) {
            System.arraycopy(shortToBytes(elem), 0, mergedArray, index, 2);
            index += 2;
        }


        byte[] compBufSizeInBytes = intToByteArray(compBufSize);

        //Здесь я записываю размер сжатого блока. Так как он в int, то записываю 4 байта
        mergedArray[index] = compBufSizeInBytes[0];
        mergedArray[++index] = compBufSizeInBytes[1];
        mergedArray[++index] = compBufSizeInBytes[2];
        mergedArray[++index] = compBufSizeInBytes[3];
        index++;

        for (byte elem : compressedBuffer) {
            mergedArray[index] = elem;
            index++;
        }
        return mergedArray;
    }

    private void CreateListOfPairs(int byteCount) {
        int i;
        short pairBytes;
        for (i = 1; i < byteCount; i += 2) {
            pairBytes = bytesToShort(buffer[i - 1], buffer[i]);
            if (!dictionary.contains(pairBytes) || dictionary.size() == 0) {
                dictionary.add(pairBytes);
            }
        }
    }

    public byte[] intToByteArray(int data) {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF));
        return result;
    }

    private void CreateCompressedBuffer(int byteCount) {
        int i;
        //int byteCount = buffer.length;
        byte ind;
        short pairBytes;
        for (i = 1; i < byteCount; i += 2) {
            pairBytes = bytesToShort(buffer[i - 1], buffer[i]);
            ind = (byte) dictionary.indexOf(pairBytes);
            compressedBuffer.add(ind);
        }
    }

    private byte[] OutputData() {
        ArrayList<Byte> out = new ArrayList<>();
        byte b1 = (byte) dictionary.size();
        out.add((byte) dictionary.size());
        for (short elem : dictionary) {
            byte[] tmp;
            tmp = shortToBytes(elem);
            out.add(tmp[0]);
            out.add(tmp[1]);
        }
        out.add((byte) compressedBuffer.size());
        out.addAll(compressedBuffer);
        byte[] res = new byte[out.size()];
        for (int i = 0; i < out.size(); i++) {
            res[i] = out.get(i);
        }
        return res;
    }

    /*    public byte[] deCompress(byte[] input) {
            int dictSize = 0, bufSize = 0, i = 0, curLen = 0;
            byte first, second;
            if (!dictionary.isEmpty() || !compressedBuffer.isEmpty()) {
                dictionary.clear();
                compressedBuffer.clear();
            }
            while (curLen < input.length) {
                dictSize = input[curLen++];
                for (i = 0; i < dictSize; i++) {
                    first = input[curLen++];
                    second = input[curLen++];
                    dictionary.add(bytesToShort(first, second));
                }
                bufSize = input[curLen++];

                for (i = 0; i < bufSize; i++) {
                    byte a = input[curLen++];
                    compressedBuffer.add(a);
                }
            }
            return OutputDataDecode();
        }

        private byte[] OutputDataDecode() {
            byte[] byteBuf;
            byte[] res = new byte[2 * compressedBuffer.size()];
            ArrayList<Byte> out = new ArrayList<>();
            int i = 0;
            for (byte elem : compressedBuffer) {
                byteBuf = shortToBytes(dictionary.get(elem));
                res[i++] = byteBuf[0];
                res[i++] = byteBuf[1];
            }
            return res;
        }*/
    public byte[] decode(byte[] inputArray, int byteCount) {
        untreatedBytes = new ArrayList<>();
        byte[] ArrayBytesDictSize = new byte[4];

        for (byte elem : inputArray)
            untreatedBytes.add(elem);

        if (dictSize == 0) {
            ArrayBytesDictSize[0] = untreatedBytes.get(0);
            ArrayBytesDictSize[1] = untreatedBytes.get(1);
            ArrayBytesDictSize[2] = untreatedBytes.get(2);
            ArrayBytesDictSize[3] = untreatedBytes.get(3);

            dictSize = ByteBuffer.wrap(ArrayBytesDictSize).getInt();
            necessarySize = dictSize * 2 + 1;
        }


        if (untreatedBytes.size() > necessarySize && compBufferSize == 0) {
            ArrayBytesDictSize[0] = untreatedBytes.get(dictSize * 2 + 4);
            ArrayBytesDictSize[1] = untreatedBytes.get(dictSize * 2 + 5);
            ArrayBytesDictSize[2] = untreatedBytes.get(dictSize * 2 + 6);
            ArrayBytesDictSize[3] = untreatedBytes.get(dictSize * 2 + 7);

            compBufferSize = ByteBuffer.wrap(ArrayBytesDictSize).getInt();
            necessarySize += compBufferSize + 1;
        }

        //if (compBufferSize != 0 && dictSize != 0 && untreatedBytes.size() >= necessarySize) {
        byte[] outputArray = deCompress();
        dictSize = 0;
        compBufferSize = 0;
        necessarySize = 0;
        dictionary.clear();
        compressedBuffer.clear();
        return outputArray;
        //}
    }

    private byte[] deCompress() {
        int i;
        byte first, second;
        byte a;


        untreatedBytes.remove(3);
        untreatedBytes.remove(2);
        untreatedBytes.remove(1);
        untreatedBytes.remove(0);

        untreatedBytes.remove(dictSize * 2 + 3);
        untreatedBytes.remove(dictSize * 2 + 2);
        untreatedBytes.remove(dictSize * 2 + 1);
        untreatedBytes.remove(dictSize * 2);


        for (i = 0; i < dictSize; i++) {
            second = untreatedBytes.remove(1);
            first = untreatedBytes.remove(0);
            dictionary.add(bytesToShort(first, second));
        }

        for (i = 0; !untreatedBytes.isEmpty() && i < compBufferSize; i++) {
            a = untreatedBytes.remove(0);
            compressedBuffer.add(a);
        }

        return CreateOutputArray();
    }

    private byte[] CreateOutputArray() {
        int arrSize = compBufferSize * 2;
        byte[] outputArray = new byte[arrSize];
        int index = 0;

        byte[] byteBuf;

        for (byte elem : compressedBuffer) {
            byteBuf = shortToBytes(dictionary.get(elem));
            outputArray[index] = byteBuf[0];
            outputArray[index + 1] = byteBuf[1];

            index += 2;
            if(index == 466)
                index = 466;
        }

        return outputArray;
    }


    private short bytesToShort(byte first, byte second) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(first);
        bb.put(second);
        return bb.getShort(0);
    }

    private byte[] shortToBytes(short value) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(value);
        return buffer.array();
    }
}

