package com;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.config.classic.ClassicPlatformConfigProvider;
import com.powsybl.dynamicsimulation.DynamicModel;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.DynamicSimulation;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.dynawaltz.dynamicmodels.GeneratorSynchronousThreeWindingsProportionalRegulations;
import com.powsybl.dynawaltz.dynamicmodels.LoadAlphaBeta;
import com.powsybl.dynawaltz.dynamicmodels.OmegaRef;
import com.powsybl.dynawaltz.events.EventQuadripoleDisconnection;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;

public class Main {
    static final Path CONFIG_PATH = Paths.get("");

    // relative to the root of the repo (can be changed to be absolute)
    public static void main(String[] args) {
        loadConfig(CONFIG_PATH);
        Network net = loadNetwork();
        DynamicSimulation.Runner dynawoSimulation = DynamicSimulation.find();
        DynamicSimulationResult results = dynawoSimulation.run(net, getMockModelSupplier(), emptyEventSupplier());
        results.getTimeLine().stream().forEach(str -> System.out.println(str.getValue()));
        System.out.println(results.isOk());
    }

    private static DynamicModelsSupplier getMockModelSupplier() {
        return network -> {
            List<DynamicModel> loadMapping = network.getLoadStream()
                    .map(load -> new LoadAlphaBeta(load.getId(), load.getId(), "LAB"))
                    .collect(Collectors.toList());
            List<DynamicModel> genMapping = network.getGeneratorStream()
                    .map(gen -> new GeneratorSynchronousThreeWindingsProportionalRegulations(gen.getId(),
                            gen.getId(), "GSTWPR"))
                    .collect(Collectors.toList());
            List<DynamicModel> omegaMapping = network.getGeneratorStream()
                    .map(gen -> new OmegaRef(gen.getId()))
                    .collect(Collectors.toList());
            genMapping.addAll(loadMapping);
            genMapping.addAll(omegaMapping);
            return genMapping;
        };
    }
    private static EventModelsSupplier getMockEventSupplier() {
        return network -> Collections.singletonList(new EventQuadripoleDisconnection("evtId", network.getGeneratorStream().findFirst().get().getId(), "EQD"));
    }
    private static DynamicModelsSupplier emptyModelSupplier() {
        return network -> new LinkedList<>();
    }
    private static EventModelsSupplier emptyEventSupplier() {
        return network -> new LinkedList<>();
    }

    private static Network loadNetwork() {
        String networkName = "IEEE14.iidm";
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
