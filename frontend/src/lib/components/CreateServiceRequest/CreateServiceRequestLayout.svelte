<script lang="ts">
	import { CreateServiceRequestSteps } from './shared';

	export let step: CreateServiceRequestSteps;

	function resolveClassName(base: string, step: CreateServiceRequestSteps) {
		return step === CreateServiceRequestSteps.LOCATION ? base + '-map-step' : base;
	}
</script>

<div class={resolveClassName('layout-container', step)}>
	<div class={resolveClassName('side-content', step)}><slot name="side-bar" /></div>
	<div class={resolveClassName('main-content', step)}><slot name="main-content" /></div>
</div>

<style>
	/* Map Step Styles */
	/* mobile */
	.layout-container-map-step {
		display: grid;
		grid-template-columns: 100%;
		height: 100%;
	}
	.main-content-map-step {
		flex-grow: 1;
		height: 100%;
	}
	.side-content-map-step {
		overflow-y: auto;
		height: 100%;
		display: none;
		position: relative;
		width: 100%;
	}

	/* tablet and above */
	@media (min-width: 768px) {
		.layout-container-map-step {
			grid-template-columns: 400px auto;
		}
		.side-content-map-step {
			display: unset;
		}
	}

	/* Other Steps Styles */
	/* mobile */
	.layout-container {
		grid-template-columns: 100%;
		height: 100%;
	}
	.main-content {
		display: none;
	}
	.side-content {
		overflow-y: auto;
		height: 100%;
	}

	/* tablet and above */
	@media (min-width: 768px) {
		.layout-container {
			display: grid;
			grid-template-columns: 400px auto;
		}
		.side-content {
			width: 100%;
		}
		.main-content {
			display: unset;
			flex-grow: 1;
			height: 100%;
		}
	}
</style>
