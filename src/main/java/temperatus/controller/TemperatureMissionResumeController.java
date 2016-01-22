package temperatus.controller;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by aquesada on 22/01/2016.
 */
public class TemperatureMissionResumeController implements Initializable {

	// This class only support temperature readers!




	@Override public void initialize(URL location, ResourceBundle resources) {

	}

	private void setUp(/*OneWireContainer container*/) throws Exception {

		DSPortAdapter adapter = null;
		Enumeration en = OneWireAccessProvider.enumerateAllAdapters();

		try {
			adapter = OneWireAccessProvider.getAdapter("", "");
		} catch(Exception e) {
			while (en.hasMoreElements())
			{
				DSPortAdapter temp = ( DSPortAdapter ) en.nextElement();

				System.out.println("Adapter: " + temp.getAdapterName());

				Enumeration f = temp.getPortNames();

				while (f.hasMoreElements())
				{
					System.out.println("   Port name : "
							+ (( String ) f.nextElement()));
				}
				return;
			}
		}

		OneWireContainer container = new OneWireContainer();
		container.setupContainer(adapter, adapter.getAddressAsLong());

		if(!(container instanceof TemperatureContainer)){
			throw new Exception();
		}

		TemperatureContainer tc = (TemperatureContainer) container;

		String iButtonModel = container.getName();
		String iButtonType = container.getAlternateNames();
		String modelDescription = container.getDescription();

		System.out.println("iButton = [Model: " + iButtonModel + ", Type: " + iButtonType + "]\n");

		double minTemperature = tc.getMinTemperature();
		double maxTemperature = tc.getMaxTemperature();

		System.out.println("\t[MinTemperature: " + minTemperature + ", MaxTemperature: " + maxTemperature + "]\n");

		try{
			adapter.beginExclusive(true);

			byte[] state = tc.readDevice();
			tc.doTemperatureConvert(state);
			double read = tc.getTemperature(state);

			System.out.println("\t[Readed temperature: " + read + "]\n");

		} finally {
			adapter.endExclusive();
		}

		if(!(container instanceof MissionContainer)){
			throw new Exception();
		}

		MissionContainer mc = (MissionContainer) container;

		try {
			adapter.beginExclusive(true);

			mc.loadMissionResults();
			int sampleRate = mc.getMissionSampleRate(0);
			int sampleCount = mc.getMissionSampleCount(0);
			int totalSampleCount = mc.getMissionSampleCountTotal(0);
			Date startDate = new Date(mc.getMissionTimeStamp(0));

			System.out.println("\t[Sample rate (s): " + sampleRate + ", Sample Count: " + sampleCount + "Total Sample Count: " + totalSampleCount + "]\n");

			for(int i = 0; i < sampleCount; i++) {
				System.out.println("\t\t " + mc.getMissionSample(0, i) + String.valueOf(new Date(mc.getMissionSampleTimeStamp(0, i))) + ", C�");
			}

		} finally {
			adapter.endExclusive();
		}


	}
}
