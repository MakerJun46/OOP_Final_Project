package csv;

import org.w3c.dom.ranges.Range;

import javax.xml.crypto.Data;
import java.lang.reflect.Type;
import java.sql.Array;
import java.util.*;
import java.util.function.Predicate;

public class TableImpl implements Table {

    private ArrayList<String> headers = new ArrayList<>();
    private ArrayList<Column> ColumnList = new ArrayList<>();
    private boolean isFirstRowHeader;
    private String AllHeaders;

    TableImpl(String header, ArrayList<String> columns)
    {
        ArrayList<ArrayList<String>> s_tmp = new ArrayList<>();

        if(header != null && !header.equals(""))
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
            for(int j = isFirstRowHeader ? 1 : 0; j < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); j++)
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
        ArrayList<String> Datas = new ArrayList<>();

        if(isFirstRowHeader)
        {
            beginIndex++;
            endIndex++;
        }

        for(int i = beginIndex; i < endIndex; i++)
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
    public Table selectRowsAt(int... indices) {
        ArrayList<String> Datas = new ArrayList<>();

        if(isFirstRowHeader)
            for(int i = 0; i < indices.length; i++)
                indices[i]++;


        for(int i : indices)
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
    public Table selectColumns(int beginIndex, int endIndex) {
        ArrayList<String> Datas = new ArrayList<>();
        String Headers = isFirstRowHeader ? "" : null;

        if(isFirstRowHeader)
        {
            for(int i = beginIndex; i < endIndex; i++)
            {
                Headers += ColumnList.get(i).getHeader();
                if(i != endIndex - 1)
                    Headers += ",";
            }
        }

        for(int i = isFirstRowHeader ? 1 : 0; i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++)
        {
            String temp = "";
            for(int j = beginIndex; j < endIndex; j++)
            {
                temp += ColumnList.get(j).getValue(i);
                if(j != endIndex - 1)
                    temp += ",";
            }
            Datas.add(temp);
        }

        Table table = new TableImpl(Headers, Datas);

        return table;
    }

    @Override
    public Table selectColumnsAt(int... indices) {
        ArrayList<String> Datas = new ArrayList<>();
        String Headers = isFirstRowHeader ? "" : null;

        if(isFirstRowHeader)
        {
            for(int i = 0; i < indices.length; i++)
            {
                Headers += ColumnList.get(indices[i]).getHeader();
                if(i != indices.length - 1)
                    Headers += ",";
            }
        }

        for(int i = isFirstRowHeader ? 1 : 0; i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++)
        {
            String temp = "";
            for(int j = 0; j < indices.length; j++)
            {
                temp += ColumnList.get(indices[j]).getValue(i);
                if(j != indices.length - 1)
                    temp += ",";
            }
            Datas.add(temp);
        }

        Table table = new TableImpl(Headers, Datas);

        return table;
    }


    /**
     * @param
     * @return 검색 조건에 해당하는 행으로 구성된 새로운 Table 생성 후 반환, 제일 나중에 구현 시도하세요.
     */
    @Override
    public <T> Table selectRowsBy(String columnName, Predicate<T> predicate) {

        ArrayList<String> Datas = new ArrayList<>();
        ArrayList<Integer> selectRowsIndex = new ArrayList<>();

        Column c = getColumn(columnName);

        for(int i = isFirstRowHeader ? 1 : 0; i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++)
        {
            try // String type lambda
            {
                if(predicate.test((T) c.getValue(i)))
                    selectRowsIndex.add(i);
            }
            catch (ClassCastException e_null)
            {
                try // Integer type lambda
                {
                    if(c.getValue(i, Integer.class) == null)
                        continue;

                    if(predicate.test((T) c.getValue(i, Integer.class)))
                        selectRowsIndex.add(i);
                }
                catch(ClassCastException e_integer) // Double type lambda
                {
                    if(c.getValue(i, Double.class) == null)
                        continue;

                    if(predicate.test((T) c.getValue(i, Double.class)))
                        selectRowsIndex.add(i);
                }
            }
        }

        // String, Integer, Double 타입 검사에서 한 케이스도 발견되지 않은 경우 Object로 간주
        // getValue에서 얻을 수 있는 type은 Stirng, Integer, Double, Object 4가지이므로 위에서 검사 후 해당 케이스가 없으면 Object 검사 진행
        // 상단 반복문에서 검사하지 못한 이유는 Object(null)로 return 받을 시 NullPointerException과 ClassCastException이 동시 발생하여 분리
        // 상단에서 한 케이스라도 발견한 경우 Object 타입이 아니므로 검사 패스
        if(selectRowsIndex.size() == 0)
        {
            for(int i = isFirstRowHeader ? 1 : 0; i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++)
            {
                try
                {
                    if(predicate.test((T) c.getValue(i, Double.class)))
                        selectRowsIndex.add(i);
                }
                catch (NullPointerException e) // 이 경우 Object 타입 lambda 식이 아니므로 상단에서 검사된 케이스가 아예 없는 경우, 바로 break
                {
                    selectRowsIndex.clear();
                    break;
                }
            }
        }

        // 선별된 행(selectRowsIndex)으로만 구성된 새로운 Table 객체 생성 및 반환
        for(int i = 0; i < selectRowsIndex.size(); i++)
        {
            String temp = "";
            for(int j = 0; j < getColumnCount(); j++)
            {
                temp += ColumnList.get(j).getValue(selectRowsIndex.get(i));
                if(j != getColumnCount() - 1)
                    temp += ",";
            }

            Datas.add(temp);
        }

        Table table = new TableImpl(AllHeaders, Datas);

        return table;
    }

    void swapRowData(ArrayList<Column> List, int moveIndex, int targetIndex)
    {

        for(int i = 0; i < getColumnCount(); i++)
        {
            String moveData = List.get(i).getValue(moveIndex);
            String targetData = List.get(i).getValue(targetIndex);

            List.get(i).setValue(targetIndex, moveData);
            List.get(i).setValue(moveIndex, targetData);
        }
    }

    void insertRowData(ArrayList<Column> List, int moveIndex, int targetIndex)
    {
        if(moveIndex == targetIndex)
            return;

        for(int i = 0; i < getColumnCount(); i++)
        {
            String moveData = List.get(i).getValue(moveIndex);

            if(targetIndex < moveIndex) // 뒤에 있는 자료를 앞으로 삽입하는 경우 (isNullFirst인 경우)
            {
                for (int j = moveIndex; j > targetIndex; j--)
                {
                    List.get(i).setValue(j, List.get(i).getValue(j - 1));
                }
            }
            else // 앞에 있는 자료를 뒤로 삽입하는 경우 (!isNullFirst인 경우)
            {
                for(int j = moveIndex; j < targetIndex; j++)
                {
                    List.get(i).setValue(j, List.get(i).getValue(j + 1));
                }
            }

            List.get(i).setValue(targetIndex, moveData); // movedata taretIndex 자리로 삽입 (targetData 삭제)
        }
    }

    @Override
    public Table sort(int byIndexOfColumn, boolean isAscending, boolean isNullFirst) {

        ArrayList<Column> Datas = ColumnList;
        int nullTmpIndex = isFirstRowHeader ? 1 : 0;
        nullTmpIndex = isNullFirst ? nullTmpIndex : (isFirstRowHeader ? getRowCount() : getRowCount() - 1);

        if(isNullFirst) {
            for (int i = isFirstRowHeader ? 1 : 0; i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++) {
                if (Datas.get(byIndexOfColumn).getValue(i).equals("null")) // null data
                {
                    insertRowData(Datas, i, nullTmpIndex);
                    nullTmpIndex++;
                }
            }
        }
        else
        {
            for(int i = isFirstRowHeader ? getRowCount() : getRowCount() - 1; i > 0; i--)
            {
                if(Datas.get(byIndexOfColumn).getValue(i).equals("null"))
                {
                    insertRowData(Datas, i, nullTmpIndex);
                }
            }
        }

        // select sort (O(n^2))
        for(int i = isNullFirst ? nullTmpIndex : (isFirstRowHeader ? 1 : 0); i < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); i++)
        {
            int sortIndex = i;

            for (int j = i + 1; j < (isFirstRowHeader ? getRowCount() + 1 : getRowCount()); j++)
            {
                try // data가 int 또는 double인 경우
                {
                    if (isAscending && Double.parseDouble(Datas.get(byIndexOfColumn).getValue(j)) <
                            Double.parseDouble(Datas.get(byIndexOfColumn).getValue(sortIndex)))
                    {
                        sortIndex = j;
                    }
                    else if(!isAscending && Double.parseDouble(Datas.get(byIndexOfColumn).getValue(j)) >
                            Double.parseDouble(Datas.get(byIndexOfColumn).getValue(sortIndex)))
                    {
                        sortIndex = j;
                    }
                }
                catch (NumberFormatException e) // data가 string 타입인 경우
                {
                    if (Datas.get(byIndexOfColumn).getValue(j).equals("null"))
                        continue;

                    if(isAscending && Datas.get(byIndexOfColumn).getValue(j).compareTo(
                            Datas.get(byIndexOfColumn).getValue(sortIndex)) < 0)
                    {
                        sortIndex = j;
                    }
                    else if(!isAscending && Datas.get(byIndexOfColumn).getValue(j).compareTo(
                            Datas.get(byIndexOfColumn).getValue(sortIndex)) > 0)
                    {
                        sortIndex = j;
                    }
                }
            }

            swapRowData(Datas, sortIndex, i);
        }

        return this;
    }

    @Override
    public Table shuffle() {

        Random rand = new Random();

        for(int i = getRowCount(); i > (isFirstRowHeader ? 1 : 0); i--)
        {
            int randIndex = rand.nextInt((isFirstRowHeader ? i - 1 : i)); // 헤더가 있는 경우 0 ~ 891로 뽑는다.

            swapRowData(ColumnList, i, (isFirstRowHeader ? randIndex + 1 : randIndex)); // 헤더가 있는 경우 0번째 라인은 바꾸면 안되니까
        }

        return this;
    }

    @Override
    public int getRowCount() { return ColumnList.get(0).count(); }

    @Override
    public int getColumnCount() { return ColumnList.size(); }

    @Override
    public Column getColumn(int index) { return ColumnList.get(index); }

    @Override
    public Column getColumn(String name) {

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(ColumnList.get(i).getHeader().equals(name))
                return ColumnList.get(i);
        }

        return null;
    }

    @Override
    public boolean fillNullWithMean() {

        boolean isNullFilled = false;

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(ColumnList.get(i).fillNullWithMean())
                isNullFilled = true;
        }

        return isNullFilled;
    }

    @Override
    public boolean fillNullWithZero() {

        boolean isNullFilled = false;

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(ColumnList.get(i).fillNullWithZero())
                isNullFilled = true;
        }

        return isNullFilled;
    }

    @Override
    public boolean standardize() {

        boolean isStandardized = false;

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(ColumnList.get(i).standardize())
                isStandardized = true;
        }

        return isStandardized;
    }

    @Override
    public boolean normalize() {

        boolean isNormalized = false;

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(ColumnList.get(i).normalize())
                isNormalized = true;
        }

        return isNormalized;
    }

    @Override
    public boolean factorize() {

        boolean isFactorized = false;

        for(int i = 0; i < getColumnCount(); i++)
        {
            if(ColumnList.get(i).factorize())
                isFactorized = true;
        }

        return isFactorized;
    }

}
