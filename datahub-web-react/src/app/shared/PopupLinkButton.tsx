import { Button } from 'antd';
import React, { useState } from 'react';
import styled from 'styled-components';

const StyledButton = styled(Button)`
    padding-left: 10px;
    padding-right: 10px;
`;

type PopupClosedEvent = {
    from: string;
    data: any;
};

type Props = {
    urn: string;
    baseUrl: string;
    entityType: string;
    popupDoneCallback: (evt: PopupClosedEvent) => void;
};

export const PopupLinkButton = ({
    urn,
    baseUrl,
    entityType,
    popupDoneCallback = (_evt: PopupClosedEvent) => {},
}: Props) => {
    const [eamButtonEnabled, setEamButtonEnabled] = useState<boolean>(true);

    return (
        <StyledButton
            disabled={!eamButtonEnabled}
            type="link"
            onClick={() => {
                // disable button to avoid double invocation
                setEamButtonEnabled(false);

                // const eamUrl = `${IntegrationCfg.IntegrationRoutes.ENTITY_ACCESS_MANAGEMENT}/${entityType}/${urn}`;
                const eamUrl = `${baseUrl}/${entityType}/${urn}`;

                const popupDoneEventCallback = (event) => {
                    if (event.data.sender === 'EAM') {
                        console.log('Received EAM event: ', event.data);
                        globalThis.removeEventListener('message', popupDoneEventCallback);
                        popupDoneCallback({
                            from: eamUrl,
                            // TODO Specify data to post to parent
                            data: event.data,
                        });
                        setEamButtonEnabled(true);
                    }
                };

                globalThis.addEventListener('message', popupDoneEventCallback);

                const popup = window.open(eamUrl, 'parent', 'height=400,width=600');

                if (popup) {
                    let popupLivenessCheckerInterval: ReturnType<typeof setInterval> | null = null;
                    const popupLivenessChecker = () => {
                        if (popup.closed && popupLivenessCheckerInterval) {
                            setEamButtonEnabled(true);
                            clearInterval(popupLivenessCheckerInterval);
                        }
                    };
                    popupLivenessCheckerInterval = setInterval(popupLivenessChecker, 1000);
                }
            }}
        >
            Entity Access Management
        </StyledButton>
    );
};
