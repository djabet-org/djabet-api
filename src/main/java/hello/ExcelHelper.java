package hello;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelHelper {

  private Logger _log = Logger.getLogger(this.getClass().getName());
  public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  static String[] HEADERs = { "Número", "Cor", "Data", "Horário" };
  static String SHEET = "Rolls";

  public static boolean hasExcelFormat(MultipartFile file) {

    if (!TYPE.equals(file.getContentType())) {
      return false;
    }

    return true;
  }

  public List<Roll> excelToRolls(InputStream is) {
    try {
      XSSFWorkbook workbook = new XSSFWorkbook(is);

      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rows = sheet.iterator();

      List<Roll> rolls = new ArrayList<Roll>();

      int rowNumber = 0;
      while (rows.hasNext()) {
        Row currentRow = rows.next();

        // skip header
          if (rowNumber < 2) {
          rowNumber++;
          continue;
        }

        Iterator<Cell> cellsInRow = currentRow.iterator();

        Roll.RollBuilder rollBuilder = Roll.builder();

        int cellIdx = 0;
        StringBuilder dateBuilder = new StringBuilder();

        while (cellsInRow.hasNext()) {
          Cell currentCell = cellsInRow.next();
          rollBuilder.platform("blaze");

          switch (cellIdx) {
          case 0:
              rollBuilder.roll((int)currentCell.getNumericCellValue());
            break;
          case 1:
            rollBuilder.color(_getColor(currentCell.getStringCellValue()));
            break;
          case 2:
          dateBuilder.append(currentCell.getStringCellValue());
            break;
          case 3:
          dateBuilder.append(" "+currentCell.getStringCellValue());
            rollBuilder.createdTime(_getDateAsTimestamp(dateBuilder.toString()));
            break;
          default:
            break;
          }

          cellIdx++;
        }

        rolls.add(rollBuilder.build());
      }

      rolls.forEach(roll -> _log.info(roll.toString()));

      workbook.close();

      return rolls;
    } catch (Throwable e) {
      _log.warning("error: "+e.getMessage());
      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
    }
  }

  private long _getDateAsTimestamp(String dateString) {
    String pattern = "dd/M/uuuu H:mm:ss";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault());
    LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateTimeFormatter);
    ZoneId zoneId = ZoneId.of("America/Recife");
    ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
    return zonedDateTime.toInstant().toEpochMilli();
  }

  private String _getColor(String value) {
    // TODO Auto-generated method stub
    switch (value) {
      case "preto":
        return "black";
      case "vermelho":
        return "red";
      default:
        return "white";
    }
  }
}