package com.pranav.temple_software.repositories;
import com.pranav.temple_software.models.Seva;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SevaRepository {

		public final Map<String, Seva> sevaMap = new LinkedHashMap<>();

		public void initializeSevaData() {
			// First 19 sevas (using first image amounts)
			double[] first19 = {
					50.00, 30.00, 50.00, 1000.00, 20.00, 10.00, 200.00, 50.00, 30.00,
					30.00, 25.00, 20.00, 100.00, 20.00, 20.00, 100.00, 50.00, 300.00
			};

			// Second 19 sevas (using second image amounts)
			double[] second18 = {
					200.00, 250.00, 350.00, 300.00, 250.00, 250.00, 100.00, 1000.00,
					100.00, 50.00, 100.00, 125.00, 250.00, 400.00, 400.00, 30.00,
					200.00, 300.00, 500.00
			};

			// First 19 sevas (items 1-19)
			sevaMap.put("1", new Seva("1", "ಬಲಿವಾಡು", first19[0]));
			sevaMap.put("2", new Seva("2", "ಪಂಚಾಮೃತಾಭಿಷೇಕ", first19[1]));
			sevaMap.put("3", new Seva("3", "ರುದ್ರಾಭಿಷೇಕ", first19[2]));
			sevaMap.put("4", new Seva("4", "ಏಕಾದಶ ರುದ್ರಾಭಿಷೇಕ", first19[3]));
			sevaMap.put("5", new Seva("5", "ಕ್ಷೀರಾಭಿಷೇಕ", first19[4]));
			sevaMap.put("6", new Seva("6", "ಅಷ್ಟೋತ್ತರ ಕುಂಕುಮಾರ್ಚನೆ", first19[5]));
			sevaMap.put("7", new Seva("7", "ಸಹಸ್ರನಾಮ ಕುಂಕುಮಾರ್ಚನೆ", first19[6]));
			sevaMap.put("8", new Seva("8", "ಕಾರ್ತಿಕ ಪೂಜೆ", first19[7]));
			sevaMap.put("9", new Seva("9", "ತ್ರಿಮಧುರ", first19[8]));
			sevaMap.put("10", new Seva("10", "ಪುಷ್ಪಾಂಜಲಿ", first19[9]));
			sevaMap.put("11", new Seva("11", "ಹಣ್ಣುಕಾಯಿ", first19[10]));
			sevaMap.put("12", new Seva("12", "ಶಾಸ್ತಾರ ದೇವರಿಗೆ ಕಾಯಿ", first19[11]));
			sevaMap.put("13", new Seva("13", "ಪಂಚಕಜ್ಜಾಯ", first19[12]));
			sevaMap.put("14", new Seva("14", "ಅಪ್ಪಕಜ್ಜಾಯ (1 ಕುಡ್ತೆ )", first19[13]));
			sevaMap.put("15", new Seva("15", "ಮಂಗಳಾರತಿ", first19[14]));
			sevaMap.put("16", new Seva("16", "ಕರ್ಪೂರಾರತಿ", first19[15]));
			sevaMap.put("17", new Seva("17", "ತುಪ್ಪದ ನಂದಾದೀಪ", first19[16]));
			sevaMap.put("18", new Seva("18", "ಎಳ್ಳೆಣ್ಣೆ ನಂದಾದೀಪ", first19[17]));

			// Last seva from first image (19th)
			sevaMap.put("19", new Seva("19", "ಒಂದು ದಿನದ ಪೂಜೆ", 300)); // Using last value from first image

			// Next 19 sevas (items 20-38)
			sevaMap.put("20", new Seva("20", "ಸರ್ವಸೇವೆ", second18[0]));
			sevaMap.put("21", new Seva("21", "ಗಣಪತಿ ಹವನ", second18[1]));
			sevaMap.put("22", new Seva("22", "ದೂರ್ವಾಹೋಮ", second18[2]));
			sevaMap.put("23", new Seva("23", "ಶನಿ ಪೂಜೆ", second18[3]));
			sevaMap.put("24", new Seva("24", "ಶನಿ ಜಪ", second18[4]));
			sevaMap.put("25", new Seva("25", "ರಾಹು ಜಪ", second18[5]));
			sevaMap.put("26", new Seva("26", "ತುಲಾಭಾರ", second18[6]));
			sevaMap.put("27", new Seva("27", "ದೀಪಾರಾಧನೆ", second18[7]));
			sevaMap.put("28", new Seva("28", "ನೈವೇದ್ಯ ಸಮರ್ಪಣೆ", second18[8]));
			sevaMap.put("29", new Seva("29", "ಹಾಲು ಪಾಯಸ", second18[9]));
			sevaMap.put("30", new Seva("30", "ಪಿಂಡಿ ಪಾಯಸ", second18[10]));
			sevaMap.put("31", new Seva("31", "ಕಠಿಣ ಪಾಯಸ", second18[11]));
			sevaMap.put("32", new Seva("32", "2 ಕಾಯಿ ಪಾಯಸ", second18[12]));
			sevaMap.put("33", new Seva("33", "5 ಕಾಯಿ ಪಾಯಸ", second18[13]));
			sevaMap.put("34", new Seva("34", "ಹೆಸರುಬೇಳೆ ಪಾಯಸ", second18[14]));
			sevaMap.put("35", new Seva("35", "ನಾಗನಿಗೆ ಹಾಲು ಸಮರ್ಪಣೆ", second18[15]));
			sevaMap.put("36", new Seva("36", "ನಾಗ ಪೂಜೆ", second18[16]));
			sevaMap.put("37", new Seva("37", "ನಾಗ ತಂಬಿಲ", second18[17]));
			sevaMap.put("38", new Seva("38", "ಪವಮಾನ ಅಭಿಷೇಕ", second18[18]));
		}
	}

