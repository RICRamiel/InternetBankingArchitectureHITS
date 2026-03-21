interface BrandLogoProps {
  size?: "sm" | "md" | "lg" | "xl";
  showText?: boolean;
}

export function BrandLogo({ size = "md", showText = true }: BrandLogoProps) {
  const sizes = {
    sm: { container: "h-6 w-6", text: "text-lg" },
    md: { container: "h-8 w-8", text: "text-2xl" },
    lg: { container: "h-12 w-12", text: "text-4xl" },
    xl: { container: "h-16 w-16", text: "text-5xl" },
  };

  const currentSize = sizes[size];

  return (
    <div className="flex items-center gap-3">
      {/* Tank Icon */}
      <div
        className={`${currentSize.container} relative flex items-center justify-center`}
      >
        <svg
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="w-full h-full"
        >
          {/* Tank body */}
          <rect
            x="4"
            y="12"
            width="20"
            height="10"
            rx="2"
            className="fill-primary stroke-primary"
            strokeWidth="1.5"
          />
          {/* Tank turret */}
          <rect
            x="10"
            y="8"
            width="8"
            height="6"
            rx="1"
            className="fill-primary stroke-primary"
            strokeWidth="1.5"
          />
          {/* Tank barrel */}
          <rect
            x="18"
            y="10"
            width="10"
            height="2"
            rx="1"
            className="fill-primary"
          />
          {/* Tank tracks - left */}
          <ellipse
            cx="9"
            cy="22"
            rx="3"
            ry="2.5"
            className="fill-primary/30 stroke-primary"
            strokeWidth="1.5"
          />
          {/* Tank tracks - right */}
          <ellipse
            cx="19"
            cy="22"
            rx="3"
            ry="2.5"
            className="fill-primary/30 stroke-primary"
            strokeWidth="1.5"
          />
        </svg>
      </div>
      
      {/* Brand Text */}
      {showText && (
        <div className="flex items-baseline gap-0">
          <span className={`${currentSize.text} font-bold text-primary tracking-tight`}>
            B
          </span>
          <span className={`${currentSize.text} font-bold text-primary/70 tracking-tight`}>
            -
          </span>
          <span className={`${currentSize.text} font-bold text-primary tracking-tight`}>
            tank
          </span>
        </div>
      )}
    </div>
  );
}
