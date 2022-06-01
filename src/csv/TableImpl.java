package csv;

import org.w3c.dom.ranges.Range;

import javax.xml.crypto.Data;
import java.lang.reflect.Type;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Predicate;

public class TableImpl implements Table {

    private ArrayList<String> headers = new ArrayList<>();
    private ArrayList<Column> ColumnList = new ArrayList<>();
    private boolean isFirstRowHeader;
    private String AllHeaders;

    TableImpl(String header, ArrayList<String> columns)
    {
        ArrayList<ArrayList<String>> s_tmp = new ArrayList<>();

        if(header != null)
        {
            AllHeaders = header;
            String[] temp = header.split(",");

            for(String s : temp)
            {
                headers.add(s);
            }
            isFirstRowHeader = true;
        }

        for(String s : columns)
        {
            String[] temp = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            ArrayList<String> al_tmp = new ArrayList<>();

            for(int i = 0; i < temp.length; i++)
            {
                if(temp[i].equals(""))
                    al_tmp.add("null");
                else
                    al_tmp.add(temp[i]);
            }

            if(s.charAt(s.length() - 1) == ',')
                al_tmp.add("null");

            s_tmp.add(al_tmp);
        }

        for(int i = 0; i < s_tmp.get(0).size(); i++) // 열의 크기
        {
            ArrayList<Object> al_tmp = new ArrayList<>();

            for(int j = 0; j < s_tmp.size(); j++) // 행의 크기
            {
                al_tmp.add(s_tmp.get(j).get(i));
            }

            Column c = new ColumnImpl(isFirstRowHeader ? headers.get(i) : null, al_tmp); // i번째 header column 생성

            ColumnList.add(c);
        }

    }

    public String toString()
    {
        String hashCode;
        String RangeIndex;
        String DataColums;
        String Datas = "";
        String space = " ";
        int [] spaces = new int[4];
        int [] dtyeps = new int[3];

        String[] headers = {"#", "Column", "Non-Null Count", "Dtype"};

        hashCode = "<" + getClass().getInterfaces()[0].getName() + "@"
                    + Integer.toHexString(hashCode()) + ">\n";

        RangeIndex = "RangeIndex: " + getRowCount() + " entries, " + "0 to " + (getRowCount() - 1) + "\n";

        DataColums = "Data Colums (total " + getColumnCount() + " colums):\n";


        for(int i = 0; i < headers.length; i++)
        {
            spaces[i] = headers[i].length();
        }

        // 1,2,3 열 공백 계산
        for(int i = 0; i < ColumnList.size(); i++)
        {
            spaces[0] = spaces[0] < Integer.toString(i).length() ? Integer.toString(i).length() : spaces[0];
            spaces[1] = spaces[1] < ColumnList.get(i).getHeader().length() ? ColumnList.get(i).getHeader().length() : spaces[1];

            if(spaces[2] < Integer.toString(ColumnList.get(i).count()).length() + " non-null".length())
                spaces[2] = Integer.toString(ColumnList.get(i).count()).length() + " non-null".length();
        }

        //header
        for(int i = 0; i < 3; i++)
        {
            Datas += space.repeat(spaces[i] - headers[i].length()) + headers[i] + " |";
        }

        Datas += space.repeat(spaces[3] - headers[3].length()) + headers[3] + "\n";

        //Datas
        for(int i = 0; i < ColumnList.size(); i++)
        {
            Datas += space.repeat(spaces[0] - Integer.toString(i).length()) + i + " |";
            Datas += space.repeat(spaces[1] - ColumnList.get(i).getHeader().length()) + ColumnList.get(i).getHeader() + " |";

            String tmp = Integer.toString((int)ColumnList.get(i).count() - (int)ColumnList.get(i).getNullCount()) + " non-null";
            Datas += space.repeat(spaces[2] - tmp.length()) + tmp + " |";

            Datas += getColumnType(ColumnList.get(i)) + "\n";

            switch (getColumnType(ColumnList.get(i)))
            {
                case "double" :
                    dtyeps[0]++;
                    break;
                case "int" :
                    dtyeps[1]++;
                    break;
                case "String" :
                    dtyeps[2]++;
                    break;
                default:
                    break;
            }
        }

        Datas += "dtypes: double(" + dtyeps[0] + "), int(" + dtyeps[1] + "), String(" + dtyeps[2] + ")";

        return hashCode + RangeIndex + DataColums + Datas;
    }

    private String getColumnType(Column column)
    {
        for(int i = 0; i < column.count(); i++)
        {
            if(i == 0 && column.getHeader() != null)
                continue;

            if(column.getValue(i).equals("null"))
                continue;

            try
            {
                Double.parseDouble(column.getValue(i));
                if(column.getValue(i).contains("."))
                    return "double";
            }
            catch (NumberFormatException e)
            {
                return "String";
            }
        }

        return "int";
    }

    @Override
    public void print() {
        String space = " ";
        int [] spaces = new int[ColumnList.size()];

        if(isFirstRowHeader) {
            for (int i = 0; i < spaces.length; i++) {
                spaces[i] = ColumnList.get(i).getHeader().length();
            }
        }
        for(int i = 0; i < spaces.length; i++)
        {
            for(int j = 0; j < getRowCount(); j++)
            {
                if(spaces[i] < ColumnList.get(i).getValue(j).length())
                    spaces[i] = ColumnList.get(i).getValue(j).length();
            }
        }

        if(isFirstRowHeader) { // header가 있으면 첫번째 라인은 헤더 표시
            for (int i = 0; i < ColumnList.size(); i++) {
                System.out.print(space.repeat(spaces[i] - ColumnList.get(i).getHeader().length()));
                System.out.print(ColumnList.get(i).getHeader() + " | ");
            }
            System.out.println();
        }

        for(int i = isFirstRowHeader ? 1 : 0; i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++) // header가 있으면 1행부터 데이터 출력
        {
            for(int j = 0; j < getColumnCount(); j++)
            {
                System.out.print(space.repeat(spaces[j] - ColumnList.get(j).getValue(i).length()));
                System.out.print(ColumnList.get(j).getValue(i) + " | ");
            }
            System.out.println();
        }

    }

    @Override
    public Table getStats() {
        String Headers = isFirstRowHeader ? "" : null;
        String[] Rows = new String[]{"count", "mean", "std", "min", "25%", "50%", "75%", "max"};

        ArrayList<String> columns = new ArrayList<>();

        if(isFirstRowHeader) { // 헤더 추가
            for (int i = 0; i < ColumnList.size(); i++) {
                if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
                {
                    Headers += "," + ColumnList.get(i).getHeader();
                }
            }
        }

        String Datas = Rows[0];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Integer.toString((int)ColumnList.get(i).getNumericCount());
            }
        }
        columns.add(Datas);

        Datas = Rows[1];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getMean());
            }
        }
        columns.add(Datas);

        Datas = Rows[2];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getStd());
            }
        }
        columns.add(Datas);

        Datas = Rows[3];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getNumericMin());
            }
        }
        columns.add(Datas);

        Datas = Rows[4];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getQ1());
            }
        }
        columns.add(Datas);

        Datas = Rows[5];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getMedian());
            }
        }
        columns.add(Datas);

        Datas = Rows[6];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getQ3());
            }
        }
        columns.add(Datas);

        Datas = Rows[7];
        for(int i = 0; i < ColumnList.size(); i++)
        {
            if (ColumnList.get(i).getNumericCount() > 0) // numberic 열이 하나라도 있으면 실행
            {
                Datas += "," + Double.toString(ColumnList.get(i).getNumericMax());
            }
        }
        columns.add(Datas);

        Table table = new TableImpl(Headers, columns);

        return table;
    }

    @Override
    public Table head() {
        ArrayList<String> Datas = new ArrayList<>();

        for(int i = isFirstRowHeader ? 1 : 0; i <= (isFirstRowHeader ? 5 : 4) ; i++)
        {
            String temp = "";
            for(int j = 0; j < ColumnList.size(); j++)
            {
                temp += ColumnList.get(j).getValue(i);
                if(j != ColumnList.size() - 1)
                    temp += ",";
            }
            Datas.add(temp);
        }

        Table table = new TableImpl(isFirstRowHeader ? AllHeaders : null, Datas);

        return table;
    }

    @Override
    public Table head(int lineCount) {
        ArrayList<String> Datas = new ArrayList<>();
        lineCount = isFirstRowHeader ? lineCount + 1 : lineCount;

        for(int i = isFirstRowHeader ? 1 : 0; i < lineCount ; i++)
        {
            String temp = "";
            for(int j = 0; j < ColumnList.size(); j++)
            {
                temp += ColumnList.get(j).getValue(i);
                if(j != ColumnList.size() - 1)
                    temp += ",";
            }
            Datas.add(temp);
        }

        Table table = new TableImpl(isFirstRowHeader ? AllHeaders : null, Datas);

        return table;
    }

    @Override
    public Table tail() {
        ArrayList<String> Datas = new ArrayList<>();

        for(int i = getRowCount() - 4; i <= getRowCount(); i++)
        {
            String temp = "";
            for(int j = 0; j < ColumnList.size(); j++)
            {
                temp += ColumnList.get(j).getValue(i);
                if(j != ColumnList.size() - 1)
                    temp += ",";
            }
            Datas.add(temp);
        }

        Table table = new TableImpl(isFirstRowHeader ? AllHeaders : null, Datas);

        return table;
    }

    @Override
    public Table tail(int lineCount) {
        ArrayList<String> Datas = new ArrayList<>();

        for(int i = getRowCount() - lineCount + 1; i <= getRowCount(); i++)
        {
            String temp = "";
            for(int j = 0; j < ColumnList.size(); j++)
            {
                temp += ColumnList.get(j).getValue(i);
                if(j != ColumnList.size() - 1)
                    temp += ",";
            }
            Datas.add(temp);
        }

        Table table = new TableImpl(isFirstRowHeader ? AllHeaders : null, Datas);

        return table;
    }

    @Override
    public Table selectRows(int beginIndex, int endIndex) {
        return null;
    }

    @Override
    public Table selectRowsAt(int... indices) {
        return null;
    }

    @Override
    public Table selectColumns(int beginIndex, int endIndex) {
        return null;
    }

    @Override
    public Table selectColumnsAt(int... indices) {
        return null;
    }

    @Override
    public <T> Table selectRowsBy(String columnName, Predicate<T> predicate) {
        return null;
    }

    @Override
    public Table sort(int byIndexOfColumn, boolean isAscending, boolean isNullFirst) {
        return null;
    }

    @Override
    public Table shuffle() {
        return null;
    }

    @Override
    public int getRowCount() { return ColumnList.get(0).count(); }

    @Override
    public int getColumnCount() {
        return ColumnList.size();
    }

    @Override
    public Column getColumn(int index) {
        return null;
    }

    @Override
    public Column getColumn(String name) {
        return null;
    }

    @Override
    public boolean fillNullWithMean() {
        return false;
    }

    @Override
    public boolean fillNullWithZero() {
        return false;
    }

    @Override
    public boolean standardize() {
        return false;
    }

    @Override
    public boolean normalize() {
        return false;
    }

    @Override
    public boolean factorize() {
        return false;
    }
}
