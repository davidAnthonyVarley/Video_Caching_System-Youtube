import java.io.FileWriter;
import java.io.IOException;

public class write_to_excel {
    public void write(int[][] data) {
        // Sample data

        // File path
        String filePath = "ALDS_PROJECT_DATA.csv";

        // Write data to the file using FileWriter
        try (FileWriter writer = new FileWriter(filePath)) {
            for (int[] rowData : data) {
                for (int i = 0; i < rowData.length; i++) {
                    writer.write(Integer.toString(rowData[i]));
                    if (i < rowData.length - 1) {
                        writer.write('\t'); // Separate values by tabs
                    }
                }
                writer.write('\n'); // Move to the next line after each row
            }
            System.out.println("Data has been written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
