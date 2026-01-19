<script lang="ts">
	export let listHidden = false;
	export let mapHidden = false;

	$: layoutContainerClass = listHidden ? 'map-only' : mapHidden ? 'list-only' : 'list-and-map';
</script>

<div class="top-level {layoutContainerClass}">
	<div class="list-view h-full">
		<slot name="list-slot" />
	</div>
	<div class="map-view">
		<slot name="map-slot" />
	</div>
</div>

<style>
	.top-level {
		height: 100%;
	}

	.list-and-map {
		display: grid;
		grid-template-columns: 400px auto;
	}

	.list-only {
		display: grid;
		grid-template-columns: auto;
	}

	.map-only {
		display: grid;
		grid-template-columns: auto;
	}

	.map-only .list-view {
		display: none;
	}

	.list-only .map-view {
		display: none;
	}

	.list-view {
		overflow-y: auto;
	}
</style>
