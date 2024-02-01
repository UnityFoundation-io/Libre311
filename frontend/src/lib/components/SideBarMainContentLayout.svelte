<script lang="ts">
	export let sideBarBreakpointActive = true;
</script>

<div class="layout-container">
	<aside class="side-bar h-full" class:collapsable-side-bar={sideBarBreakpointActive}>
		<slot name="side-bar" />
	</aside>
	<div class="main-content"><slot name="main-content" /></div>
</div>

<style>
	.layout-container {
		grid-template-columns: 100%;
		height: 100%;
	}
	.main-content {
		flex-grow: 1;
		height: 100%;
	}

	.collapsable-side-bar {
		display: none;
	}

	.side-bar {
		position: relative;
		width: 400px;
	}

	aside {
		overflow-y: auto;
	}

	@media (min-width: 769px) {
		.layout-container {
			display: grid;
			grid-template-columns: 400px auto;
		}
		.collapsable-side-bar {
			display: unset;
		}
	}
</style>
