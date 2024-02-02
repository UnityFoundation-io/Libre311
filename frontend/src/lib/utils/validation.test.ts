import { describe, it, expect } from 'vitest';
import {
	createInput,
	optionalEmailValidator,
	emailValidator,
	optionalCoalesceNameValidator,
	optionalCoalescePhoneNumberValidator
} from './validation';

describe('emailValidator', () => {
	it('empty string is invalid', () => {
		const res = emailValidator(createInput(''));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('');
	});
	it('improper format is invalid', () => {
		const res = emailValidator(createInput('asdflk'));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('asdflk');
	});
	it('valid email passes validation', () => {
		const res = emailValidator(createInput('test@example.com'));

		expect(res.value).toBe('test@example.com');
	});
});

describe('optionalEmailValidator', () => {
	it('no input is valid', () => {
		const res = optionalEmailValidator(createInput());
		expect(res.type).toBe('valid');
		expect(res.value).toBe(undefined);
	});

	it('empty string is invalid', () => {
		const res = optionalEmailValidator(createInput(''));

		expect(res.type).toBe('invalid');
	});
	it('improper format is invalid', () => {
		const res = optionalEmailValidator(createInput('asdflk'));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('asdflk');
	});
	it('valid email passes validation', () => {
		const res = optionalEmailValidator(createInput('test@example.com'));
		expect(res.value).toBe('test@example.com');
	});
});

describe('optionalCoalesceNameValidator', () => {
	it('no input is valid', () => {
		const res = optionalCoalesceNameValidator(createInput());
		expect(res.type).toBe('valid');
		expect(res.value).toBe(undefined);
	});
	it('empty string is valid', () => {
		const res = optionalCoalesceNameValidator(createInput(''));
		expect(res.type).toBe('valid');
	});
	it('Numeric characters are invalid', () => {
		const res = optionalCoalesceNameValidator(createInput('4444'));
		expect(res.type).toBe('invalid');
		expect(res.error).toBe('Invalid');
	});
	it('valid name passes validation', () => {
		const res = optionalCoalesceNameValidator(createInput('Santiago Peña'));
		expect(res.type).toBe('valid');
		expect(res.value).toBe('Santiago Peña');
	});
});

describe('optionalCoalescePhoneNumberValidator', () => {
	it('no input is valid', () => {
		const res = optionalCoalescePhoneNumberValidator(createInput());
		expect(res.type).toBe('valid');
		expect(res.value).toBe(undefined);
	});
	it('empty string is valid', () => {
		const res = optionalCoalescePhoneNumberValidator(createInput(''));
		expect(res.type).toBe('valid');
	});
	// https://github.com/ruimarinho/google-libphonenumber/blob/master/test/phone-util_test.js
	const validNumbers = [
		'202-456-1414',
		'(202) 456-1414',
		'+1 (202) 456-1414',
		'202.456.1414',
		'202/4561414',
		'1 202 456 1414',
		'+12024561414',
		'1 202-456-1414'
	];
	for (const validNum of validNumbers) {
		it(`phone number: ${validNum} passes`, () => {
			const res = optionalCoalescePhoneNumberValidator(createInput(validNum));
			expect(res.type).toBe('valid');
			expect(res.value).toBe(validNum);
		});
	}

	const invalidNumbers = ['123456', '111111111111111111111', '63622286'];

	for (const invalidNum of invalidNumbers) {
		it(`phone number: ${invalidNum} fails`, () => {
			const res = optionalCoalescePhoneNumberValidator(createInput(invalidNum));
			expect(res.type).toBe('invalid');
			expect(res.error).toBeTruthy();
		});
	}
});
