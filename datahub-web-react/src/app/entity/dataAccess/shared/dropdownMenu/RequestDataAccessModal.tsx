import React from 'react';

import { Step, StepperModal } from '../stepper/StepperModal';
import { GenericFormConfirmationStep, GenericFormStep } from '../stepper/StepForm';

type Props = {
    onCloseModal: () => void;
    onOk: () => void;
    title?: string;
};

export const RequestDataAccessModal = ({ onCloseModal, onOk, title }: Props) => {
    const modalTitle = title || 'Create a data access request';

    const steps: Step[] = [
        {
            title: 'Requirements',
            content: GenericFormStep,
            fieldGroups: {
                purpose: {
                    inputFields: {
                        purpose: {
                            defaultValue: '',
                            multiValue: false,
                            inputType: 'textarea',
                            label: 'Purpose of access',
                            help: 'How/why do you intend to use the data?',
                            required: true,
                        },
                    },
                    label: 'Purpose of access',
                    type: 'fields-group',
                },
            },
            confirmation: GenericFormConfirmationStep,
            key: 0,
        },
        {
            title: 'Target',
            content: GenericFormStep,
            fieldGroups: {
                target: {
                    inputFields: {
                        project: {
                            defaultValue: '',
                            multiValue: false,
                            inputType: 'textfield',
                            label: 'Project ID',
                            required: true,
                            rules: [
                                {
                                    pattern: new RegExp(/^[a-z][-a-z0-9]{4,28}[a-z0-9]{1}$/gm),
                                    message: 'Project Id is not valid',
                                },
                            ],
                            normalize: (value, _, __) => value.trim(),
                            tooltipInfo:
                                'The Project must exist in Google Cloud Platform. Should start with a lowercase letter (a-z). Can include lowercase letters (a-z), numbers (0-9), and hyphens (-). Be between 6 and 30 characters long. End with a lowercase letter (a-z) or a number (0-9).',
                        },
                        datasetName: {
                            defaultValue: '',
                            multiValue: false,
                            inputType: 'textfield',
                            label: 'Dataset Name',
                            required: true,
                            rules: [
                                {
                                    max: 1024,
                                    message: 'Dataset name can contain up to 1024',
                                },
                                {
                                    pattern: new RegExp(/^[a-zA-Z0-9_]*$/gm),
                                    message:
                                        'Dataset name cannot contain spaces or special characters such as -, &, @, or %.',
                                },
                            ],
                            normalize: (value, _, __) => value.trim(),
                            tooltipInfo:
                                'The Dataset must exist in Google Cloud Platform in the same project as specified above.',
                        },
                        view: {
                            defaultValue: '',
                            multiValue: false,
                            inputType: 'textfield',
                            label: 'View Name',
                            required: true,
                            rules: [
                                {
                                    pattern: new RegExp(/^[\p{L}\p{M}\p{N}\p{Pc}\p{Pd}\p{Zs}]+$/gu),
                                    message:
                                        'View name cannot contain spaces or special characters such as &, @, or %.',
                                },
                            ],
                        },
                    },
                    label: 'Data view to be provisioned',
                    type: 'fields-group',
                },
                principal: {
                    inputFields: {
                        principalId: {
                            defaultValue: '',
                            multiValue: false,
                            inputType: 'principalId',
                            label: 'Principal Identifier',
                            required: true,
                            rules: [
                                {
                                    pattern: new RegExp(
                                        /^(serviceAccount|group):[a-z]([-a-z0-9]*[a-z0-9])?@([a-z](?:[-a-z0-9]*[a-z0-9])?\.)+[a-z]{2,}$/gu,
                                    ),
                                    message: 'Principal identifier is not valid',
                                },
                            ],
                        },
                    },
                    label: 'Authorized principal',
                    type: 'fields-group',
                },
            },
            confirmation: GenericFormConfirmationStep,
            key: 1,
        },
    ];

    return <StepperModal title={modalTitle} onCloseModal={onCloseModal} onOk={onOk} steps={steps} />;
};
