import { BgText } from "../back-ground-text/BgText";
import { LinkButton } from "../link-button/LinkButton";
import { SingleSpaceLayout } from "../../layouts/single-space-layout/SingleSpaceLayout";

export type HttpStatusScreenProps = {
  code: string;
  actionText: string;
  onAction: () => void;
  message?: string;
  /** e.g. React error boundary stack / message */
  detailText?: string;
};

export function HttpStatusScreen({
  code,
  actionText,
  onAction,
  message,
  detailText,
}: HttpStatusScreenProps) {
  const bodyClass = "text-info color-info";
  return (
    <main
      className="bg-background"
      style={{
        position: "relative",
        minHeight: "100vh",
        boxSizing: "border-box",
      }}
    >
      <BgText text={code} />
      <SingleSpaceLayout>
        <>
          {message !== undefined && message.length > 0 ? (
            <p
              className={bodyClass}
              style={{
                margin: 0,
                marginBottom: "1rem",
                textAlign: "center",
                maxWidth: "28rem",
              }}
            >
              {message}
            </p>
          ) : null}
          {detailText !== undefined && detailText.length > 0 ? (
            <pre
              className={bodyClass}
              style={{
                margin: 0,
                marginBottom: "1rem",
                textAlign: "left",
                maxWidth: "28rem",
                whiteSpace: "pre-wrap",
                wordBreak: "break-word",
                fontFamily: "inherit",
                fontSize: "0.8125rem",
                letterSpacing: "0.05em",
              }}
            >
              {detailText}
            </pre>
          ) : null}
          <LinkButton
            text={actionText}
            onClick={onAction}
            variant="info"
            textClassName="text-info"
          />
        </>
      </SingleSpaceLayout>
    </main>
  );
}
