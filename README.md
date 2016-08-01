## SNRG Configuration Frontend
This is an application designed to improve the ease of use in designing simulations for the SNRG social network simulator.  

It is a GUI application consisting of three main packages: `ui`, the package which produces the GUI, using JavaFX; `model`, which the `ui` package interfaces with and which manages the internal structure of the experiment; and `persistence`, which takes data from the `model` and produces persistent data for storage or use in the simulator. For now, this output is a collection of JSON files.

The application currently allows the creation of Nodes, Pathogens, and Edges, and the configuration of their settings.  After that, there are Agregate Settings, Simulation Configurations, and a few other properties that can be built.

For testing the software without building it, binary packages are located in `bin\Application.[build date].jar`, and these files will be updated regularly.
