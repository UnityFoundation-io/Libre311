<script lang="ts">
	export let sideBarHidden = false;
	export let mainContentHidden = false;

	$: layoutContainerClass = `layout-container-${sideBarHidden}-${mainContentHidden}`;
</script>

<div class={layoutContainerClass}>
	<div class="side-bar h-full">
		<slot name="side-bar" />
	</div>
	<div class="main-content">
		<slot name="main-content" />
	</div>
</div>

<style>
	.layout-container-false-false {
		display: grid;
		grid-template-columns: 400px auto;
		height: 100%;
	}

	.layout-container-false-true {
		display: grid;
		grid-template-columns: auto;
		height: 100%;
	}

	.layout-container-true-false {
		display: grid;
		grid-template-columns: auto;
		height: 100%;
	}

	.layout-container-true-false .side-bar {
		display: none;
	}

	.layout-container-false-true .main-content {
		display: none;
	}

	.side-bar {
		overflow-y: auto;
	}

	.main-content {
		height: 100%;
	}
</style>
