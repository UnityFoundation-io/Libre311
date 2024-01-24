<div class="is-mobile"><slot name="is-mobile" /></div>
<div class="is-desktop"><slot name="is-desktop" /></div>

<style>
	.is-desktop {
		display: none;
	}
	.is-mobile {
		display: unset;
	}
	@media (min-width: 64rem) {
		.is-mobile {
			display: none;
		}
		.is-desktop {
			display: unset;
		}
	}
</style>
