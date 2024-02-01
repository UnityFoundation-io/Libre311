<div class="is-mobile"><slot name="is-mobile" /></div>
<div class="is-mobile-or-tablet"><slot name="is-mobile-or-tablet" /></div>
<div class="is-tablet"><slot name="is-tablet" /></div>

<div class="is-desktop"><slot name="is-desktop" /></div>

<style>
	.is-desktop,
	.is-mobile,
	.is-tablet,
	.is-mobile-or-tablet {
		display: none;
	}
	/* Phone */ /* Phone */
	@media only screen and (max-width: 480px) {
		.is-mobile,
		.is-mobile-or-tablet {
			display: unset;
		}
	}

	/* Tablet */
	@media only screen and (min-width: 481px) and (max-width: 768px) {
		.is-tablet,
		.is-mobile-or-tablet {
			display: unset;
		}
	}

	/* Desktop */
	@media only screen and (min-width: 769px) {
		.is-desktop {
			display: unset;
		}
	}
</style>
