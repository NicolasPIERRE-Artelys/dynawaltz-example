package com;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.powsybl.config.classic.ClassicPlatformConfigProvider;
import com.powsybl.dynamicsimulation.DynamicSimulation;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.groovy.CurveGroovyExtension;
import com.powsybl.dynamicsimulation.groovy.DynamicModelGroovyExtension;
import com.powsybl.dynamicsimulation.groovy.EventModelGroovyExtension;
import com.powsybl.dynamicsimulation.groovy.GroovyCurvesSupplier;
import com.powsybl.dynamicsimulation.groovy.GroovyDynamicModelsSupplier;
import com.powsybl.dynamicsimulation.groovy.GroovyEventModelsSupplier;
import com.powsybl.dynamicsimulation.groovy.GroovyExtension;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;

public class Groovy {
        public static void main(String[] args) {
                loadConfig(Main.CONFIG_PATH);
                Network network = loadNetwork();

                // Load the dynamic models mapping
                GroovyDynamicModelsSupplier dynamicModelsSupplier = new GroovyDynamicModelsSupplier(
                                Paths.get("groovy/dynamicModels.groovy"),
                                GroovyExtension.find(DynamicModelGroovyExtension.class, "DynaWaltz"));

                // Load the events
                GroovyEventModelsSupplier eventModelsSupplier = new GroovyEventModelsSupplier(
                                Paths.get("groovy/eventModels.groovy"),
                                GroovyExtension.find(EventModelGroovyExtension.class, "DynaWaltz"));

                // Configure the curves
                GroovyCurvesSupplier curvesSupplier = new GroovyCurvesSupplier(
                                Paths.get("groovy/curves.groovy"),
                                GroovyExtension.find(CurveGroovyExtension.class, "DynaWaltz"));

                // Load the parameters
                DynamicSimulationParameters parameters = DynamicSimulationParameters.load();

                // Run the simulation and display the results
                DynamicSimulationResult result = DynamicSimulation.run(network, dynamicModelsSupplier,
                                eventModelsSupplier,
                                curvesSupplier, parameters);
                System.out.println(result.isOk());
                System.out.println(result.getLogs());
        }

        private static Network loadNetwork() {
                String networkName = "IEEE14.iidm";
                final InputStream is = Main.class.getClassLoader().getResourceAsStream(networkName);
                return Importers.loadNetwork(networkName, is);
        }

        /**
         * Uses ClassicPlatformConfigProvider class to load plateform config
         * Thus we modify the system variables that will be used to find config file
         * 
         * @see ClassicPlatformConfigProvider#getPlatformConfig()
         */
        private static void loadConfig(Path configPath) {
                System.setProperty("powsybl.config.dirs", configPath.toAbsolutePath().toString());
                System.setProperty("powsybl.config.name", "config"); // will try to find first matching extension : .yml
                                                                     // .xml
                                                                     // .property
        }

}
