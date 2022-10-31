package com;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.config.classic.ClassicPlatformConfigProvider;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.DynamicSimulation;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.dynawaltz.dynamicmodels.LoadAlphaBeta;
import com.powsybl.dynawaltz.events.EventQuadripoleDisconnection;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Main {
    static final Path CONFIG_PATH = Paths.get(""); // relative to the root of the repo (can be changed to be absolute)
    public static void main(String[] args) {
        loadConfig(CONFIG_PATH);
        Network net = loadNetwork();
        DynamicModelsSupplier supplier = getMockModelSupplier();
        DynamicSimulation.Runner dynawoSimulation = DynamicSimulation.find();
        DynamicSimulationResult results = dynawoSimulation.run(net, supplier, getMockEventSupplier());
        results.getTimeLine().stream().forEach(str -> System.out.println(str.getValue()));
        System.out.println(results.isOk());
    }

    private static DynamicModelsSupplier getMockModelSupplier() {
        return network -> Collections.singletonList(new LoadAlphaBeta("BBM_" + network.getLoadStream().findFirst().get().getId(), network.getLoadStream().findFirst().get().getId(), "LAB"));
    }
    private static EventModelsSupplier getMockEventSupplier() {
        return network -> Collections.singletonList(new EventQuadripoleDisconnection("evtId", network.getGeneratorStream().findFirst().get().getId(), "EQD"));
    }

    private static Network loadNetwork() {
        String networkName = "network.xml";
        final InputStream is = Main.class.getClassLoader().getResourceAsStream(networkName);
        return Importers.loadNetwork(networkName, is);
    }

    private static void loadConfigOld(Path configPath) {
        // setting plateform conf by hand
        // deprecated find a way to load a conf file given its path
        PlatformConfig.setDefaultConfig(new PlatformConfig(
                PlatformConfig.loadModuleRepository(configPath, "config"), configPath));
    }

    /**
     * Uses ClassicPlatformConfigProvider class to load plateform config
     * Thus we modify the system variables that will be used to find config file
     * @see ClassicPlatformConfigProvider#getPlatformConfig()
     */
    private static void loadConfig(Path configPath) {
        System.setProperty("powsybl.config.dirs", configPath.toAbsolutePath().toString());
        System.setProperty("powsybl.config.name", "config"); // will try to find first matching extension : .yml .xml .property
    }


}
