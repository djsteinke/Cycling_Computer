package rnfive.djs.cyclingcomputer.define;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldDef {
    private static final int[][] map = {
            {1,3},
            {6,1},
            {6,2},
            {6,3}
    };

    private List<Field> fields;
    private final int size;

    public FieldDef() {
        fields = new ArrayList<>();
        setFieldArray();
        size = fields.size();
    }

    private void setFieldArray() {
        int row = 0;
        final int w = 6;
        for (int[] m : map) {
            int sz = w/m[1];
            for (int r=0 ; r < m[0] ; r++) {
                for (int c = 0; c < m[1]; c++) {
                    fields.add(new Field(row,c*sz, sz));
                }
                row ++;
            }
        }
    }

    private int getFieldArraySize() {
        int size = 0;
        for (int[] m : map) {
            size += m[0]*m[1];
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 1;
        int l = fields.size();
        for (Field f : fields) {
            sb.append(f);
            if (i < l)
                sb.append(",");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    public int[][] toArray() {
        int[][] ret = new int[fields.size()][Field.count];
        int i = 0;
        for (Field f : fields)
            ret[i++] = f.toArray();
        return ret;
    }

    @Getter
    @Setter
    public static class Field {
        private int row;
        private int start;
        private int size;
        static final int count = 3;

        public Field() {}
        Field(int row, int start, int size) {
            this.row = row;
            this.start = start;
            this.size = size;
        }

        @Override
        public String toString() {
            return "{" + row + "," + start + "," + size + "}";
        }

        public int[] toArray() {
            return new int[] {row, start, size};
        }
    }
}
