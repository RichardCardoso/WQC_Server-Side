package com.richard.weger.wqc.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.repository.DrawingRefRepository;
import com.richard.weger.wqc.repository.ProjectRepository;

@Service
public class ItemReportHelper {
	
	@Autowired private DrawingRefRepository drawingRefRepository;
	@Autowired private ProjectRepository projectRepository;
	
	@SuppressWarnings("serial")
	public List<Item> getDefaultItems(ItemReport itemReport) {
		List<Item> itemList = new ArrayList<Item>() {
			{
				int x = 1;
				add(new Item(x++, "Geräteaufbau laut Zeichnung"));
				add(new Item(x++, "Abmessungen der Geräte lt. Zeichnung"));
				add(new Item(x++, "Liefersektionen laut Zeichnung "));
				add(new Item(x++, "Ansaug- und Ausblasöffnungen lt. Zchng. "));
				add(new Item(x++, "Bedienseite laut Zeichnung "));
				add(new Item(x++, "Position Registeranschlüsse lt. Zchng. "));
				add(new Item(x++, "Position Kondensatanschlüsse lt. Zchng. "));
				add(new Item(x++, "Reihenfolge der Register laut Zeichnung "));
				add(new Item(x++, "Registeranschlussdurchmesser lt. Datenblatt "));
				add(new Item(x++, "Laufrichtung Flachriemen"));
				add(new Item(x++, "Zugänglichkeit Motorklemmkasten "));
				add(new Item(x++, "Gerät innen späne- und schmutzfrei gereinigt "));
				add(new Item(x++, "Gerät aussen gereinigt "));
				add(new Item(x++, "Wannen späne- und schmutzfrei gereinigt "));
				add(new Item(x++, "Grundrahmen lt. Zeichnung "));
				add(new Item(x++, "Gerätefüsse lt. Zeichnung "));
				add(new Item(x++, "Kranlaschen montiert "));
				add(new Item(x++, "Klappen leichtgängig "));
				add(new Item(x++, "Funktionstest Klappen: vollständig öffen- u. schliessbar "));
				add(new Item(x++, "alle Hinweisschilder aufgeklebt"));
				add(new Item(x++, "Regelgerät für Rotortauscher vorhanden"));
				add(new Item(x, "Innenverbindungslaschen bei Ansaugwänden und Klappen vorhanden "));
			}
		};

		addPicturesName(itemList, itemReport);

		return itemList;
	}

	private void addPicturesName(List<Item> itemList, ItemReport itemReport) {
		for (Item item : itemList) {
			String fileName = generatePictureName(itemReport, item);
			item.getPicture().setFileName(fileName);
		}
	}

	public String generatePictureName(ItemReport itemReport, Item item) {
		StringBuilder sb = new StringBuilder();
		DrawingRef dRef = itemReport.getParent();
		Project project = dRef.getParent();
		sb.append(project.getReference());
		sb.append("Z");
		sb.append(dRef.getDnumber());
		sb.append("T");
		sb.append(dRef.getParts().get(0).getNumber());
		sb.append("Q");
		sb.append(item.getNumber());
		sb.append(".jpg");
		return sb.toString();
	}

	public boolean updateItem(Report r, Item item) {
		if (r instanceof ItemReport) {
			ItemReport ir = (ItemReport) r;
			for (int i = 0; i < ir.getItems().size(); i++) {
				if (ir.getItems().get(i).getId() == item.getId()) {
					ir.getItems().set(i, item);
					return true;
				}
			}
		}
		return false;
	}

	public String getXlsFileName(ItemReport report) {
		DrawingRef dRef = drawingRefRepository.getById(report.getParent().getId());
		Project project = projectRepository.getById(dRef.getParent().getId());
		return "Z".concat(String.valueOf(dRef.getDnumber())).concat("-")
				.concat(project.getReference()).concat("Q").concat(".xls");
	}
}
