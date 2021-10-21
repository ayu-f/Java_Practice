public class Match { // структура для алгоритма архивирования
    int pos; // позиция элемента
    char next; // следующий элемент
    byte next1;

    Match(int pos, char next){
        this.next = next;
        this.pos = pos;
    }

    public int getPos(){
        return pos;
    }
    public char getNext(){
        return next;
    }
}
