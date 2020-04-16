





const available_instances_json_file	= "../../instances.json";
const available_solver_stats_json_file	= "../../available_solver_stats.json";

(function() {

	var instances = JSON.parse(available_instances_json_file);

	alert(instances);
	d3.json(available_instances_json_file).then(function (instance_data) {

		/* X Axis */	
		var labels = instance_data.instances;

		d3.json(available_solver_stats_json_file).then(function (solver_data) {

			var chart_config = {};
			var datasets = [];

			console.log(solver_data);		

			for (solver in solver_data.solvers) {
				d3.json()
				datasets.push({
					label: solver.name,
					backgroundColor: window.chartColors.beautiblue,
					borderColor: window.chartColors.beautiblue,
					data
				});
			}

			var ctx = document.getElementById('canvas').getContext('2d');
			window.myLine = new Chart(ctx, chart_config);
		});
	});
})();


