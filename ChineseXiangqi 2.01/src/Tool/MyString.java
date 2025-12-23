package Tool;

import java.util.ArrayList;

public class MyString {
    private ArrayList<Character> str;

    public MyString(String a) {
        str = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) {
            str.add(a.charAt(i));
        }
    }

    public MyString() {
        //todo
        //new an empty str
        new ArrayList<>();
    }

    public void add(char x) {
        //todo
        //add x to the rear of str
        str.add(x);
    }

    public void set(int index, char x) {
        //todo
        //change the index-th character of str to x
        str.set(index, x);
    }

    public void setByString(String string) {
        for (int i = 0; i < string.length(); i++) {
            str.add(string.charAt(i));
        }
    }

    public char charAt(int index) {
        //todo
        return str.get(index);
    }

    public int length() {
        //todo
        return str.size();
    }

    public boolean equals(MyString b) {
        //todo
        if (b.length() != this.length())
            return false;
        else {
            for (int i = 0; i < str.size(); i++) {
                if (b.charAt(i) != this.charAt(i))
                    return false;
            }
            return true;
        }
    }

    public char[] toCharArray() {
        //todo
        char[] charArray = new char[this.length()];

        for (int i = 0; i < this.length(); i++) {
            charArray[i] = this.charAt(i);
        }

        return charArray;
    }

    public String toString() {
        String string = "";
        for (int i = 0; i < str.size(); i++) {
            string += str.get(i);
        }
        return string;
    }

    public void print() {
        for (int i = 0; i < str.size(); i++) {
            System.out.print(str.get(i));
        }
    }

}
