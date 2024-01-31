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
		display: flex;
		grid-template-columns: 400px auto;
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

	@media (min-width: 64rem) {
		.collapsable-side-bar {
			display: unset;
		}
	}
</style>
