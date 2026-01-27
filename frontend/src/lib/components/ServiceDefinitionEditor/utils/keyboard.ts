export function shouldSave(event: KeyboardEvent): boolean {
	return (event.ctrlKey || event.metaKey) && event.key === 's';
}

export function shouldCancel(event: KeyboardEvent): boolean {
	return event.key === 'Escape';
}

export function handleEditorKeydown(
	event: KeyboardEvent,
	actions: {
		onSave?: () => void;
		onCancel?: () => void;
		canSave?: boolean;
		isDirty?: boolean;
	}
) {
	// Save on Ctrl+S / Cmd+S
	if (shouldSave(event)) {
		event.preventDefault();
		if (actions.canSave && actions.onSave) {
			actions.onSave();
		}
	}
	// Cancel on Escape
	if (shouldCancel(event)) {
		if (actions.isDirty && actions.onCancel) {
			actions.onCancel();
		}
	}
}
