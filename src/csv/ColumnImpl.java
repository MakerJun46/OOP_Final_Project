package csv;

import javax.xml.crypto.Data;
import java.util.*;

public class ColumnImpl implements Column {

    private ArrayList<Object> DataList = new ArrayList<Object>();
    private boolean isFirstHeader = false;
    private String header;

    ColumnImpl(String header, ArrayList<Object> data)
    {
        if(header != null)
        {
            this.header = header;
            DataList.add(header);
            isFirstHeader = true;
        }

        for(int i = 0; i < data.size(); i++)
        {
            DataList.add(data.get(i));
        }
    }

    @Override
    public String getHeader() {
        if(isFirstHeader)
            return DataList.get(0).toString();
        else
            return null;
    }

    @Override
    public String getValue(int index) {
        return DataList.get(index).toString();
    }

    @Override
    public <T extends Number> T getValue(int index, Class<T> t) {
        return null;
    }

    @Override
    public void setValue(int index, String value) {
        try
        {
            DataList.set(index, value);
        }
        catch (IndexOutOfBoundsException e)
        {
            DataList.add(index, value);
        }

    }

    @Override
    public <T extends Number> void setValue(int index, T value) {
        try
        {
            DataList.set(index, value.toString());
        }
        catch (IndexOutOfBoundsException e)
        {
            DataList.add(index, value.toString());
        }
    }

    @Override
    public int count() {
        return isFirstHeader ? DataList.size() - 1 : DataList.size();
    }

    @Override
    public void print() {

    }

    @Override
    public boolean isNumericColumn() {
        for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
        {
            if(DataList.get(i).toString().equals("null"))
                continue;

            try {
                Double.parseDouble(DataList.get(i).toString());
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getNullCount() {
        return Collections.frequency(DataList, "null");
    }

    @Override
    public long getNumericCount() {
        int cnt = 0;

        for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
        {
            try
            {
                Double.parseDouble(DataList.get(i).toString());
                cnt++;
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }

        return cnt;
    }

    @Override
    public double getNumericMin() {
        if(getNumericCount() == 0) // 숫자가 없으면 0 리턴
            return 0;

        double min = Double.MAX_VALUE;

        for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
        {
            try
            {
                if(Double.parseDouble(DataList.get(i).toString()) < min)
                    min = Double.parseDouble(DataList.get(i).toString());
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }

        return min;
    }

    @Override
    public double getNumericMax() {
        if(getNumericCount() == 0) // 숫자가 없으면 0 리턴
            return 0;

        double max = 0;

        for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
        {
            try {
                if(max < Double.parseDouble(DataList.get(i).toString()))
                    max = Double.parseDouble(DataList.get(i).toString());
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }

        return max;
    }

    @Override
    public double getMean() {
        Double sum = 0.0;

        for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
        {
            try
            {
                sum += Double.parseDouble(DataList.get(i).toString());
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }

        return Math.round(sum / getNumericCount() * 1000000) / 1000000.0;
    }

    @Override
    public double getStd() {
        double sum = 0;
        double mean = getMean();

        for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
        {
            try
            {
                sum += Math.pow(Double.parseDouble(DataList.get(i).toString()) - mean, 2);
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }

        return Math.round(Math.sqrt(sum/(getNumericCount()-1))*1000000)/1000000.0;
    }

    @Override
    public double getQ1() {

        Vector<Double> temp = new Vector<>();

        for(int i = 1; i < DataList.size(); i++)
        {
            try
            {
                temp.add(Double.parseDouble(DataList.get(i).toString()));
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }

        for(int i = 0; i < temp.size() - 1; i++)
        {
            for (int j = i + 1; j < temp.size(); j++)
            {
                if (temp.get(i) > temp.get(j))
                {
                    double k = temp.get(i);
                    temp.set(i, temp.get(j));
                    temp.set(j, k);
                }
            }
        }

        return temp.get((int)getNumericCount() / 4);
    }

    @Override
    public double getMedian() {

        Vector<Double> temp = new Vector<>();

        for(int i = 1; i < DataList.size(); i++)
        {
            try {
                temp.add(Double.parseDouble(DataList.get(i).toString()));
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }
        for(int i = 0; i <temp.size() - 1; i++) {
            for (int j = i + 1; j < temp.size(); j++) {
                if (temp.get(i) > temp.get(j)) {
                    double k = temp.get(i);
                    temp.set(i, temp.get(j));
                    temp.set(j, k);
                }
            }
        }

        return temp.get((int)getNumericCount()/2);
    }


    @Override
    public double getQ3() {
        Vector<Double> temp = new Vector<>();

        for(int i = 1; i < DataList.size(); i++)
        {
            try {
                temp.add(Double.parseDouble(DataList.get(i).toString()));
            }
            catch (NumberFormatException e)
            {
                continue;
            }
        }
        for(int i = 0; i <temp.size() - 1; i++) {
            for (int j = i + 1; j < temp.size(); j++) {
                if (temp.get(i) > temp.get(j)) {
                    double k = temp.get(i);
                    temp.set(i, temp.get(j));
                    temp.set(j, k);
                }
            }
        }

        return temp.get(3 * (int)getNumericCount() / 4);
    }

    @Override
    public boolean fillNullWithMean() {

        boolean isNullFilled = false;

        if(isNumericColumn() && getNullCount() > 0)
        {
            isNullFilled = true;

            double mean = getMean();

            for (int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++) {
                if(DataList.get(i).equals("null"))
                    setValue(i, mean);
            }
        }

        return isNullFilled;
    }

    @Override
    public boolean fillNullWithZero() {

        boolean isNullFilled = false;

        if(isNumericColumn() && getNullCount() > 0)
        {
            isNullFilled = true;

            for (int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++) {
                if(DataList.get(i).equals("null"))
                    setValue(i, 0);
            }
        }

        return isNullFilled;
    }

    @Override
    public boolean standardize() {

        boolean isStandardized = false;

        if(isNumericColumn())
        {
            isStandardized = true;

            double mean = getMean();
            double std = getStd();

            for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
            {
                if(DataList.get(i).equals("null"))
                    continue;

                double data = Double.parseDouble(DataList.get(i).toString());

                DataList.set(i, Double.toString(Math.round((data - mean) / std * 1000000) / 1000000.0));
            }
        }

        return isStandardized;
    }

    @Override
    public boolean normalize() {

        boolean isNormalized = false;

        if(isNumericColumn())
        {
            isNormalized = true;

            double max = getNumericMax();
            double min = getNumericMin();

            for(int i = isFirstHeader ? 1 : 0; i < DataList.size(); i++)
            {
                if(DataList.get(i).equals("null"))
                    continue;

                double data = Double.parseDouble(DataList.get(i).toString());

                DataList.set(i, Double.toString(Math.round((data - min) / (max - min) * 1000000) / 1000000.0));
            }
        }

        return isNormalized;
    }

    @Override
    public boolean factorize() {

        boolean isFactorized = false;

        String tmp1 = getValue(isFirstHeader ? 1 : 0);
        String tmp2;

        for(int i = isFirstHeader ? 2 : 1; i < DataList.size(); i++)
        {
            if(!getValue(i).equals("null") && !tmp1.equals(getValue(i)))
            {
                tmp2 = getValue(i); // tmp1과 다른 두번째 값 추출
                break;
            }
        }


        return false;
    }

}
