import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Executor {
    int bufferSize;
    int dictSize;
    Executor(int bufferSize) {
        this.bufferSize = bufferSize;
        dictSize = bufferSize; /*******************DELETE THIS******************/
    }

    // преобразования структуры match алгоритма в массив битов
    byte[] MatchToByteArray(ArrayList<Match> match) {
        String matchStr = "";
        for (Match mt : match) {
            matchStr = matchStr.concat(Integer.toString(mt.getPos()));
            matchStr = matchStr.concat(Character.toString(mt.getNext()));
        }
        return matchStr.getBytes(StandardCharsets.UTF_8);
    }

    // encode
    // input: array of bytes, count of readen bytes
    public byte[] Encode(byte[] bytes) {
        HashMap<String, Integer> dict = new HashMap<String, Integer>(dictSize);

        ArrayList<Match> match = new ArrayList<Match>();
        char[] arrChar = new String(bytes).toCharArray();
        char[] ir = new char[bytes.length];
        ArrayList<Byte> out = new ArrayList<>();
        //for (int i = 0; i < bytes.length; i++) {
            //ir = Base64.getEncoder().encodeToString(bytes).toCharArray();
            //arrChar[i] = (char) (bytes[i] & 0xFF);
        //}
        String buffer = "";

        for (int i = 0; i < arrChar.length - 1 && arrChar[i] != 0; i++) {
            if (dict.containsKey(buffer + arrChar[i])) {
                buffer += arrChar[i];
            }
            else {
                match.add(new Match(dict.getOrDefault(buffer, 0), arrChar[i]));

                byte[] tmp = intToByteArray(dict.getOrDefault(buffer, 0));
                for (int j = 0; j < 4; j++) {
                    out.add(tmp[j]);
                }
                //byte tmp1 = (byte)arrChar[i];
                //out.add(tmp1);

                ByteBuffer b = ByteBuffer.allocate(2);
                b.putChar(arrChar[i]);
                tmp = b.array();
                out.add(tmp[0]);
                out.add(tmp[1]);

                dict.put(buffer + arrChar[i], dict.size() + 1);
                buffer = "";
            }
        }
        if (buffer.length() > 1) {
            char last_ch = buffer.charAt(buffer.length() - 1);
            match.add(new Match(dict.getOrDefault(buffer.substring(0, buffer.length() - 1), 0), last_ch));

            byte[] tmp = intToByteArray(dict.getOrDefault(buffer.substring(0, buffer.length() - 1), 0));
            for (int j = 0; j < 4; j++) {
                out.add(tmp[j]);
            }

            ByteBuffer b = ByteBuffer.allocate(2);
            b.putChar(last_ch);
            tmp = b.array();
            out.add(tmp[0]);
            out.add(tmp[1]);
        }
        byte[] res = new byte[out.size()];
        int i = 0;
        for(Byte o : out){
            res[i++] = o;
        }
        return MatchToByteArray(match);
    }

    /*void IntToBytes(int i, ArrayList<Byte> bytes){
        bytes.add((byte) (i >> 24));
        bytes.add((byte) (i >> 16));
        bytes.add((byte) (i >> 8));
        bytes.add((byte) (i));

        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i);
        int m = ByteBuffer.wrap(result).getInt();
    }

    public Byte[] Encode(byte[] bytes, int countReadenBytes) {
        HashMap<String, Integer> dict = new HashMap<String, Integer>();
        HashMap<ArrayList<Byte>, Integer> dict1 = new HashMap<ArrayList<Byte>, Integer>();

        ArrayList<Match> match = new ArrayList<Match>();
        //char[] arrChar = new String(bytes).toCharArray();
        ArrayList<Byte> buffer = new ArrayList<Byte>();
        ArrayList<Byte> out = new ArrayList<Byte>();

        for (int i = 0; i < bytes.length - 1 && bytes[i] != 0; i++) {
            buffer.add(bytes[i]);
            if (dict1.containsKey(buffer)) {
                //buffer.add(bytes[i]);
            }
            else {
                buffer.remove(buffer.size()-1);
                //match.add(new Match(dict1.getOrDefault(buffer, 0), bytes[i]));
                IntToBytes(dict1.getOrDefault(buffer, 0), out);
                out.add(bytes[i]);
                dict1.put(buffer, dict.size() + 1);
                buffer.clear();
                //buffer = "";
            }
        }
        if (buffer.size() > 1) {
            byte last_bt = buffer.get(buffer.size() - 1);
            buffer.remove(buffer.size() - 1);
            //match.add(new Match(dict1.getOrDefault(buffer, 0), last_bt));
            IntToBytes(dict1.getOrDefault(buffer, 0), out);
            out.add(last_bt);
        }

        return out.;//MatchToByteArray(match);
    }*/

    public byte[] intToByteArray(int data) {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF));
        return result;
    }

    // нахождение в подстроке числа
    int ParseInt(String str, int index){
        String num = "";
        char s = str.charAt(index);
        int i = index;
        while(str.charAt(i) >= '0' && str.charAt(i) <= '9'){
            num += str.charAt(i);
            i++;
        }

        return Integer.parseInt(num);
    }

    ArrayList<Match> ByteArrayToMatch(byte[] bytes) {
        ArrayList<Match> match = new ArrayList<Match>();
        String matchStr = new String(bytes);
        for (int i = 0; i < matchStr.length() - 1 && matchStr.charAt(i) != 0;) {
            int lenPos = 1;
            String str = matchStr.substring(i);
            int pos = ParseInt(matchStr, i);
            if(pos != 0)
                lenPos = (int)(Math.log10(pos) + 1);

            i += lenPos;
            char next = matchStr.charAt(i);
            match.add(new Match(pos, next));
            i++;
        }
        return match;
    }

    public byte[] Decode(byte[] bytes) {
        ArrayList<String> dict = new ArrayList<>();
        ArrayList<Match> match = ByteArrayToMatch(bytes);
        dict.add("");
        String ans = "";

        for (Match mt : match) {
            String word = dict.get(mt.getPos()) + mt.getNext();
            ans = ans.concat(word);
            dict.add(word);
        }
        return ans.getBytes(StandardCharsets.UTF_8);
    }
}