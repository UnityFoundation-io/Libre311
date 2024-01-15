<div class="layout-container">
	<aside class="side-content"><slot name="side-bar" /></aside>
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
		max-height: calc(100vh - var(--header-height));
	}

	.side-content {
		width: 400px;
		display: none;
	}

	aside {
		overflow-y: auto;
		max-height: calc(100vh - var(--header-height));
	}

	@media (min-width: 64rem) {
		.side-content {
			display: unset;
		}
	}
</style>
