package csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CSVs {
    /**
     * @param isFirstLineHeader csv 파일의 첫 라인을 헤더(타이틀)로 처리할까요?
     */
    public static Table createTable(File csv, boolean isFirstLineHeader) throws FileNotFoundException {

        ArrayList<String> columns = new ArrayList<>();
        String header = null;
        Scanner scanner = new Scanner(csv);

        if(isFirstLineHeader)
        {
            header = scanner.nextLine();
        }

        while(scanner.hasNextLine())
        {
            columns.add(scanner.nextLine());
        }

        Table table = new TableImpl(header, columns);

        return table;
    }

    /**
     * @return 새로운 Table 객체를 반환한다. 즉, 첫 번째 매개변수 Table은 변경되지 않는다.
     */
    public static Table sort(Table table, int byIndexOfColumn, boolean isAscending, boolean isNullFirst) {

        String Headers = "";

        for(int i = 0; i < table.getColumnCount(); i++)
        {
            Headers += table.getColumn(i).getHeader();
            if(table.getColumn(i).getHeader() != null && i != table.getColumnCount() - 1)
                Headers += ",";
        }

        ArrayList<String> Datas = new ArrayList<>();

        for(int i = Headers.equals("") ? 0 : 1; i < (Headers.equals("") ? table.getRowCount() + 1 : table.getRowCount()); i++)
        {
            String temp = "";

            for(int j = 0; j < table.getColumnCount(); j++)
            {
                temp += table.getColumn(j).getValue(i);

                if(j != table.getColumnCount() - 1)
                    temp += ",";
            }

            Datas.add(temp);
        }

        Table t = new TableImpl(Headers, Datas);
        t.sort(byIndexOfColumn, isAscending, isNullFirst);

        return t;
    }

    /**
     * @return 새로운 Table 객체를 반환한다. 즉, 첫 번째 매개변수 Table은 변경되지 않는다.
     */
    public static Table shuffle(Table table) {

        String Headers = "";

        for(int i = 0; i < table.getColumnCount(); i++)
        {
            Headers += table.getColumn(i).getHeader();
            if(table.getColumn(i).getHeader() != null && i != table.getColumnCount() - 1)
                Headers += ",";
        }

        ArrayList<String> Datas = new ArrayList<>();

        for(int i = Headers.equals("") ? 0 : 1; i < (Headers.equals("") ? table.getRowCount() + 1 : table.getRowCount()); i++)
        {
            String temp = "";

            for(int j = 0; j < table.getColumnCount(); j++)
            {
                temp += table.getColumn(j).getValue(i);

                if(j != table.getColumnCount() - 1)
                    temp += ",";
            }

            Datas.add(temp);
        }

        Table t = new TableImpl(Headers, Datas);
        t.shuffle();

        return t;
    }
}
